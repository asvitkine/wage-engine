package info.svitkine.alexei.wage;

import java.util.ArrayList;
import java.util.List;


public class Script {
	private byte[] data;

	private Callbacks callbacks;
	private World world;
	private int loopCount;
	private String inputText;
	private Object inputClick;
	private int index;
	private Object evalResult;
	private boolean handled;

	public Script(byte[] data) {
		this.data = data;
	}
	
	private static class Operand {
		public static final int OBJ = 0;
		public static final int CHR = 1;
		public static final int SCENE = 2;
		public static final int NUMBER = 3;
		public static final int STRING = 4;
		public static final int CLICK_INPUT = 5;
		public static final int TEXT_INPUT = 6;
		
		public Object value;
		public int type;
		
		public Operand(Object value, int type) {
			this.value = value;
			this.type = type;
		}
	}

	private Operand readOperand() {
		Operand result = null;
		System.out.printf("=> %x\n", data[index]);
		if (data[index] == (byte) 0xA0) { // TEXT$
			result = new Operand(inputText, Operand.TEXT_INPUT);
		} else if (data[index] == (byte) 0xA1) {
			result = new Operand(inputClick, Operand.CLICK_INPUT);
		} else if (data[index] == (byte) 0xC0) { // STORAGE@
			result = new Operand(world.getStorageScene(), Operand.SCENE);
		} else if (data[index] == (byte) 0xC1) { // SCENE@
			result = new Operand(world.getPlayer().getCurrentScene(), Operand.SCENE);
		} else if (data[index] == (byte) 0xC2) { // PLAYER@
			result = new Operand(world.getPlayer(), Operand.CHR);
		} else if (data[index] == (byte) 0xC3) { // MONSTER@
			result = new Operand(callbacks.getMonster(), Operand.CHR);
		} else if (data[index] == (byte) 0xC4) { // RANDOMSCN@
			Scene[] scenes = world.getScenes().values().toArray(new Scene[0]);
			result = new Operand(scenes[(int) (Math.random()*scenes.length)], Operand.SCENE);
		} else if (data[index] == (byte) 0xC5) { // RANDOMCHR@
			Chr[] chrs = world.getChrs().values().toArray(new Chr[0]);
			result = new Operand(chrs[(int) (Math.random()*chrs.length)], Operand.CHR);
		} else if (data[index] == (byte) 0xC6) { // RANDOMOBJ@
			Obj[] objs = world.getObjs().values().toArray(new Obj[0]);
			result = new Operand(objs[(int) (Math.random()*objs.length)], Operand.OBJ);
		} else if (data[index] == (byte) 0xB0) { // VISITS#
			result = new Operand(world.getPlayerContext().getVisits(), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB1) {
			// RANDOM# for Star Trek, but VISITS# for some other games?
			result = new Operand(1 + (int) (Math.random()*100), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB5) { // RANDOM#
			// A random number between 1 and 100.
			result = new Operand(1 + (int) (Math.random()*100), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB2) { // LOOP#
			result = new Operand(loopCount, Operand.NUMBER);
		} else if (data[index] == (byte) 0xB3) { // VICTORY#
			result = new Operand(world.getPlayerContext().getKills(), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB4) { // BADCOPY#
			result = new Operand(0, Operand.NUMBER); // ????
		} else if (data[index] == (byte) 0xFF) {
			// user variable
			int value = data[++index];
			if (value < 0) value += 256;
			// TODO: Verify that we're using the right index.
			result = new Operand(world.getPlayerContext().getUserVariable(value), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD0) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_STR_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD1) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_HIT_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD2) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_ARM_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD3) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_ACC_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD4) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_STR_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD5) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_HIT_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD6) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_ARM_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD7) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_ACC_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD8) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_SPE_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE0) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_STR_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE1) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_HIT_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE2) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_ARM_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE3) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_ACC_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE4) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_STR_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE5) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_HIT_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE6) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_ARM_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE7) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.SPIR_ACC_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE8) {
			result = new Operand(world.getPlayerContext().getStatVariable(Context.PHYS_SPE_CUR), Operand.NUMBER);
		} else if (Character.isDefined(data[index])) {
			result = readStringOperand();
			index--;
		} else {
			System.out.printf("Dunno what %x is (index=%d)!\n", data[index], index);
		}
		index++;
		return result;
	}
	
	private Operand readStringOperand() {
		Operand result;
		StringBuilder sb = new StringBuilder();
		boolean allDigits = true;
		while (Character.isDefined(data[index])) {
			char c = (char) data[index++];
			if (!Character.isDigit(c))
				allDigits = false;
			sb.append(c);
		}
		if (allDigits && sb.length() > 0) {
			System.out.println("Read number " + sb.toString());
			result = new Operand(new Integer(sb.toString()), Operand.NUMBER);
		} else {
			// TODO: This string could be a room name or something like that.
			System.out.println("Read string " + sb.toString());
			result = new Operand(sb.toString(), Operand.STRING);
		}
		return result;
	}

	private String readOperator() {
		String op = null;
		if (data[index] == (byte) 0x81) {
			op = "=";
		} else if (data[index] == (byte) 0x82) {
			op = "<";
		} else if (data[index] == (byte) 0x83) {
			op = ">";
		} else if (data[index] == (byte) 0x8F) {
			op = "+";
		} else if (data[index] == (byte) 0x90) {
			op = "-";
		} else if (data[index] == (byte) 0x91) {
			op = "*";
		} else if (data[index] == (byte) 0x92) {
			op = "/";
		} else if (data[index] == (byte) 0x93) {
			op = "==";
		} else if (data[index] == (byte) 0x94) {
			op = ">>";
		} else {
			System.out.printf("OP is %x\n", data[index]);
		}
		index++;
		return op;
	}

	private Boolean evalClickEquality(Operand lhs, Operand rhs) {
		Boolean result = null;
		if (lhs.value == null || rhs.value == null) {
			result = false;
		} else if (lhs.value == rhs.value) {
			result = true;
		} else if (rhs.type == Operand.STRING) {
			if (lhs.value instanceof Chr) {
				result = ((Chr) lhs.value).getName().equalsIgnoreCase(rhs.value.toString());
			} else if (lhs.value instanceof Obj) {
				result = ((Obj) lhs.value).getName().equalsIgnoreCase(rhs.value.toString());
			}
		}
		return result;
	}

	// returns Boolean so that NPE can be detected (on invalid op)
	private Boolean eval(Operand lhs, String op, Operand rhs) {
		Boolean result = null;
		if (op.equals("=")) {
			if (lhs.type == Operand.CLICK_INPUT) {
				result = evalClickEquality(lhs, rhs);
			} else if (rhs.type == Operand.CLICK_INPUT) {
				result = evalClickEquality(rhs, lhs);
			} else {
				List<PairEvaluator> handlers = new ArrayList<PairEvaluator>();
				handlers.add(new PairEvaluator(Operand.NUMBER, Operand.NUMBER) {
					@Override
					public void evaluatePair(Operand o1, Operand o2) {
						short left = ((Number) o1.value).shortValue();
						short right = ((Number) o2.value).shortValue();
						evalResult = (left == right);
					}
				});
				handlers.add(new PairEvaluator(Operand.OBJ, Operand.SCENE) {
					@Override
					public void evaluatePair(Operand o1, Operand o2) {
						evalResult = ((Scene) o2.value).getObjs().contains((Obj) o1.value);
					}
				});
				handlers.add(new PairEvaluator(Operand.CHR, Operand.SCENE) {
					@Override
					public void evaluatePair(Operand o1, Operand o2) {
						evalResult = ((Scene) o2.value).getChrs().contains((Chr) o1.value);
					}
				});
				handlers.add(new PairEvaluator(Operand.OBJ, Operand.CHR) {
					@Override
					public void evaluatePair(Operand o1, Operand o2) {
						evalResult = ((Chr) o2.value).getInventory().contains((Obj) o1.value);
					}
				});
				handlers.add(new PairEvaluator(Operand.STRING, Operand.TEXT_INPUT) {
					@Override
					public void evaluatePair(Operand o1, Operand o2) {
						if (inputText != null) {
							evalResult = inputText.toLowerCase().contains(((String) o1.value).toLowerCase());
						} else {
							evalResult = false;
						}
					}
				});
				handlers.add(new PairEvaluator(Operand.TEXT_INPUT, Operand.STRING) {
					@Override
					public void evaluatePair(Operand o1, Operand o2) {
						if (inputText != null) {
							evalResult = inputText.toLowerCase().contains(((String) o2.value).toLowerCase());
						} else {
							evalResult = false;
						}
					}
				});
				evaluatePair(handlers, lhs, rhs);
				result = (Boolean) evalResult;
			}
		} else if (op.equals("<")) {
			// less than
			// does not equal
			// does not have
			List<PairEvaluator> handlers = new ArrayList<PairEvaluator>();
			handlers.add(new PairEvaluator(Operand.NUMBER, Operand.NUMBER) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					short left = ((Number) o1.value).shortValue();
					short right = ((Number) o2.value).shortValue();
					evalResult = (left < right);
				}
			});
			handlers.add(new PairEvaluator(Operand.STRING, Operand.TEXT_INPUT) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					if (inputText != null) {
						evalResult = !inputText.toLowerCase().contains(((String) o1.value).toLowerCase());
					} else {
						evalResult = false;
					}
				}
			});
			handlers.add(new PairEvaluator(Operand.TEXT_INPUT, Operand.STRING) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					if (inputText != null) {
						evalResult = !inputText.toLowerCase().contains(((String) o2.value).toLowerCase());
					} else {
						evalResult = false;
					}
				}
			});
			handlers.add(new PairEvaluator(Operand.OBJ, Operand.CHR) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					Obj o = (Obj) o1.value;
					Chr c = (Chr) o2.value;
					evalResult = (o.getCurrentOwner() != c);
				}
			});
			handlers.add(new PairEvaluator(Operand.OBJ, Operand.SCENE) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					Obj o = (Obj) o1.value;
					Scene s = (Scene) o2.value;
					evalResult = (o.getCurrentScene() != s);
				}
			});
			evaluatePair(handlers, lhs, rhs);
			result = (Boolean) evalResult;
		} else if (op.equals(">")) {
			// greater than
			// does not equal
			// does not have
			List<PairEvaluator> handlers = new ArrayList<PairEvaluator>();
			handlers.add(new PairEvaluator(Operand.NUMBER, Operand.NUMBER) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					short left = ((Number) o1.value).shortValue();
					short right = ((Number) o2.value).shortValue();
					evalResult = (left > right);
				}
			});
			handlers.add(new PairEvaluator(Operand.STRING, Operand.STRING) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					if (o1.value == null || o2.value == null) {
						evalResult = (o1.value == o2.value);
					} else {
						evalResult = o1.value.equals(o2.value);
					}
				}
			});
			handlers.add(new PairEvaluator(Operand.OBJ, Operand.CHR) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					Obj o = (Obj) o1.value;
					Chr c = (Chr) o2.value;
					evalResult = (o.getCurrentOwner() != c);
				}
			});
			handlers.add(new PairEvaluator(Operand.OBJ, Operand.SCENE) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					Obj o = (Obj) o1.value;
					Scene s = (Scene) o2.value;
					evalResult = (o.getCurrentScene() != s);
				}
			});
			handlers.add(new PairEvaluator(Operand.CHR, Operand.SCENE) {
				@Override
				public void evaluatePair(Operand o1, Operand o2) {
					Chr c = (Chr) o1.value;
					Scene s = (Scene) o2.value;
					evalResult = (c != null && c.getCurrentScene() != s);
				}
			});
			evaluatePair(handlers, lhs, rhs);
			result = (Boolean) evalResult;
		} else if (op.equals("==") || op.equals(">>")) {
			// TODO: check if >> can be used for click inputs and if == can be used for other things
			if (op.equals("==") && lhs.type == Operand.CLICK_INPUT) {
				result = evalClickEquality(lhs, rhs);
			} else if (op.equals("==") && rhs.type == Operand.CLICK_INPUT) {
				result = evalClickEquality(rhs, lhs);
			} else {
				// exact string match
				if (lhs.type == Operand.TEXT_INPUT) {
					if (rhs.type != Operand.STRING || inputText == null) {
						result = false;
					} else {
						result = inputText.toLowerCase().equals(((String) rhs.value).toLowerCase());
					}
				} else if (rhs.type == Operand.TEXT_INPUT) {
					if (lhs.type != Operand.STRING || inputText == null) {
						result = false;
					} else {
						result = ((String) lhs.value).toLowerCase().equals(inputText.toLowerCase());
					}
				}
				if (op.equals(">>")) {
					result = !result;
				}
			}
		}
		if (result == null) {
			System.err.printf("OMG UNHANDLED CASE FIXME (op is %s, lhs.type is %d, rhs.type is %d)\n", op, lhs.type, rhs.type);
			System.err.println("LHS IS " + lhs.value);
			System.err.println("RHS IS " + rhs.value);
			result = false;
		}
		return result;
	}

	private void skipIf() {
		do {
			readOperand();
			readOperator();
			readOperand();
		} while (data[index++] != (byte) 0xFE);
	}
	
	private void skipBlock() {
		int fromIndex = index;
		int nesting = 1;
		while (index < data.length) {
			if (data[index] == (byte) 0x80) { // IF
				nesting++;
				index++;
				skipIf();
				index--;
			} else if (data[index] == (byte) 0x88 || data[index] == (byte) 0x87) { // END or EXIT
				nesting--;
				if (nesting == 0) {
					index++;
					System.out.println("Skipped lines " + indexToLine(fromIndex) + " to " + indexToLine(index));
					return;
				}
			} else switch (data[index]) {
				case (byte) 0x8B: // PRINT
				case (byte) 0x8C: // SOUND
				case (byte) 0x8E: // LET
				case (byte) 0x95: // MENU
					while (data[index] != (byte) 0xFD) {
						index++;
					}
			}
			index++;
		}
	}

	public static interface Callbacks {
		public void appendText(String text, Object... args);
		public void playSound(String sound);
		public void setMenu(String menuData);
		public void performAttack(Chr attacker, Chr victim, Weapon weapon);
		public void regen();
		public Chr getMonster();
		public Obj getOffer();
		public void setAim(int aim);
		public void setCommandWasQuick();
	}

	private void processIf() {
		int then = index;
		while (data[then] != (byte) 0xFE)
			then++;
		System.out.println("I love conditionals! " + buildStringFromOffset(index - 1, then - index) + "}");
		int logicalOp = 0; // 0 => initial, 1 => and, 2 => or 
		boolean result = true;
		boolean done = false;
		do {
			Operand lhs = readOperand();
			String op = readOperator();
			Operand rhs = readOperand();
			System.out.print("eval lhs => " + lhs.value);
			System.out.println(" rhs => " + rhs.value + " op => " + op);
			boolean condResult = eval(lhs, op, rhs);
			if (logicalOp == 1) {
				result = (result && condResult);
			} else if (logicalOp == 2) {
				result = (result || condResult);
			} else { // logicalOp == 0
				result = condResult;
			}
			if (data[index] == (byte) 0x84) {
				logicalOp = 1; // and
			} else if (data[index] == (byte) 0x85) {
				logicalOp = 2; // or
			} else if (data[index] == (byte) 0xFE) {
				done = true; // then
			}
			if (!done)
				System.out.printf("not done => %x\n", data[index]);
			index++;
		} while (!done);
		System.out.println("Result = " + result);
		if (result == false) {
			skipBlock();
		}
	}
	
	private void assign(int index, short value) {
		Context context = world.getPlayerContext();
		if (data[index] == (byte) 0xFF) { // user variable
			int var = data[++index];
			if (var < 0) var += 256;
			context.setUserVariable(var, value);
		} else if (data[index] == (byte) 0xD0) {
			context.setStatVariable(Context.PHYS_STR_BAS, value);
		} else if (data[index] == (byte) 0xD1) {
			context.setStatVariable(Context.PHYS_HIT_BAS, value);
		} else if (data[index] == (byte) 0xD2) {
			context.setStatVariable(Context.PHYS_ARM_BAS, value);
		} else if (data[index] == (byte) 0xD3) {
			context.setStatVariable(Context.PHYS_ACC_BAS, value);
		} else if (data[index] == (byte) 0xD4) {
			context.setStatVariable(Context.SPIR_STR_BAS, value);
		} else if (data[index] == (byte) 0xD5) {
			context.setStatVariable(Context.SPIR_HIT_BAS, value);
		} else if (data[index] == (byte) 0xD6) {
			context.setStatVariable(Context.SPIR_ARM_BAS, value);
		} else if (data[index] == (byte) 0xD7) {
			context.setStatVariable(Context.SPIR_ACC_BAS, value);
		} else if (data[index] == (byte) 0xD8) {
			context.setStatVariable(Context.PHYS_SPE_BAS, value);
		} else if (data[index] == (byte) 0xE0) {
			context.setStatVariable(Context.PHYS_STR_CUR, value);
		} else if (data[index] == (byte) 0xE1) {
			context.setStatVariable(Context.PHYS_STR_CUR, value);
		} else if (data[index] == (byte) 0xE2) {
			context.setStatVariable(Context.PHYS_ARM_CUR, value);
		} else if (data[index] == (byte) 0xE3) {
			context.setStatVariable(Context.PHYS_ACC_CUR, value);
		} else if (data[index] == (byte) 0xE4) {
			context.setStatVariable(Context.SPIR_STR_CUR, value);
		} else if (data[index] == (byte) 0xE5) {
			context.setStatVariable(Context.SPIR_HIT_CUR, value);
		} else if (data[index] == (byte) 0xE6) {
			context.setStatVariable(Context.SPIR_ARM_CUR, value);
		} else if (data[index] == (byte) 0xE7) {
			context.setStatVariable(Context.SPIR_ACC_CUR, value);
		} else if (data[index] == (byte) 0xE8) {
			context.setStatVariable(Context.PHYS_SPE_CUR, value);
		} else {
			System.err.printf("No idea what I'm supposed to assign! (%x at %d)!\n", data[index], index);
		}
	}

	private void processLet() {
		String lastOp = null;
		short result = 0;
		int oldIndex = index;
		readOperand(); // skip LHS
		index++; // skip "=" operator
		do {
			Operand operand = readOperand();
			// TODO assert that value is NUMBER
			short value = ((Number) operand.value).shortValue();
			if (lastOp != null) {
				if (lastOp.equals("+"))
					result += value;
				else if (lastOp.equals("-"))
					result -= value;
				else if (lastOp.equals("/"))
					result = (short) (value == 0 ? 0 : result / value);
				else if (lastOp.equals("*"))
					result *= value;
			} else {
				result = value;
			}
			if (data[index] == (byte) 0xFD)
				break;
			lastOp = readOperator();
		} while (true);
		System.out.println("processLet " + buildStringFromOffset(oldIndex - 1, index - oldIndex + 1) + "}");
		assign(oldIndex, result);
		index++;
	}
	
	private static abstract class PairEvaluator {
		public int lhsType;
		public int rhsType;
		public PairEvaluator(int lhsType, int rhsType) {
			this.lhsType = lhsType;
			this.rhsType = rhsType;
		}
		public abstract void evaluatePair(Operand o1, Operand o2);
	}

	private Operand convertOperand(Operand operand, int type) {
		if (operand.type == type)
			return operand;

		if (type == Operand.SCENE) {
			if (operand.type == Operand.STRING || operand.type == Operand.NUMBER) {
				String key = operand.value.toString().toLowerCase();
				Scene scene = world.getScenes().get(key);
				if (scene != null) {
					return new Operand(scene, Operand.SCENE);
				}
			}
		} else if (type == Operand.OBJ) {
			if (operand.type == Operand.STRING || operand.type == Operand.NUMBER) {
				String key = operand.value.toString().toLowerCase();
				Obj obj = world.getObjs().get(key);
				if (obj != null) {
					return new Operand(obj, Operand.OBJ);
				}
			} else if (operand.type == Operand.CLICK_INPUT) {
				if (inputClick instanceof Obj) {
					return new Operand(inputClick, Operand.OBJ);
				}
			}
		} else if (type == Operand.CHR) {
			if (operand.type == Operand.STRING || operand.type == Operand.NUMBER) {
				String key = operand.value.toString().toLowerCase();
				Chr chr = world.getChrs().get(key);
				if (chr != null) {
					return new Operand(chr, Operand.CHR);
				}
			} else if (operand.type == Operand.CLICK_INPUT) {
				if (inputClick instanceof Chr) {
					return new Operand(inputClick, Operand.CHR);
				}
			}
		}

		return null;
	}
	
	private void evaluatePair(List<PairEvaluator> handlers, Operand o1, Operand o2) {
		evalResult = null;
		// First, try for exact matches.
		System.out.println("Trying exact matches on " + o1.type + " " + o2.type);
		for (PairEvaluator e : handlers) {
			if (e.lhsType == o1.type && e.rhsType == o2.type) {
				e.evaluatePair(o1, o2);
				return;
			}
		}
		// Now, try partial matches.
		System.out.println("Trying partial matches on " + o1.type + " " + o2.type);
		for (PairEvaluator e : handlers) {
			Operand converted;
			if (e.lhsType == o1.type && (converted = convertOperand(o2, e.rhsType)) != null) {
				e.evaluatePair(o1, converted);
				return;
			} else if (e.rhsType == o2.type && (converted = convertOperand(o1, e.lhsType)) != null) {
				e.evaluatePair(converted, o2);
				return;
			}
		}
		// Now, try double conversion.
		System.out.println("Trying double conversion on " + o1.type + " " + o2.type);
		for (PairEvaluator e : handlers) {
			Operand c1, c2;
			if ((c1 = convertOperand(o1, e.lhsType)) != null &&
				(c2 = convertOperand(o2, e.rhsType)) != null)
			{
				e.evaluatePair(c1, c2);
				return;
			}
		}
	}
	
	private void processMove() {
		System.out.println("processMove!");
		Operand what = readOperand();
		// TODO check data[index] == 0x8A
		index++;
		Operand to = readOperand();
		// TODO check data[index] == 0xFD
		index++;
		List<PairEvaluator> handlers = new ArrayList<PairEvaluator>();
		handlers.add(new PairEvaluator(Operand.OBJ, Operand.CHR) {
			@Override
			public void evaluatePair(Operand o1, Operand o2) {
				Obj obj = (Obj) o1.value;
				Chr chr = (Chr) o2.value;
				world.move(obj, chr);
			}
		});
		handlers.add(new PairEvaluator(Operand.OBJ, Operand.SCENE) {
			@Override
			public void evaluatePair(Operand o1, Operand o2) {
				Obj obj = (Obj) o1.value;
				Scene scene = (Scene) o2.value;
				world.move(obj, scene);
			}
		});
		handlers.add(new PairEvaluator(Operand.CHR, Operand.SCENE) {
			@Override
			public void evaluatePair(Operand o1, Operand o2) {
				Chr chr = (Chr) o1.value;
				Scene scene = (Scene) o2.value;
				world.move(chr, scene);
				// TODO: if its a bad guy, he should attack etc...
			}
		});
		evaluatePair(handlers, what, to);
	}

	private void appendText(String str, Object... args) {
		handled = true;
		callbacks.appendText(str, args);
	}
	
	public boolean execute(World world, int loopCount,
			String inputText, Object inputClick,
			Callbacks callbacks)
	{
		this.world = world;
		this.loopCount = loopCount;
		this.inputText = inputText;
		this.inputClick = inputClick;
		this.callbacks = callbacks;
		this.handled = false;
		try {
			index = 12;
			while (index < data.length) {
				if (data[index] == (byte) 0x80) { // IF{
					index++;
					processIf();
				} else if (data[index] == (byte) 0x87) { // EXIT
					System.err.println("exit at line " + indexToLine(index));
					return handled;
				} else if (data[index] == (byte) 0x89) { // MOVE
					index++;
					Scene currentScene = world.getPlayer().getCurrentScene();
					processMove();
					if (world.getPlayer().getCurrentScene() != currentScene)
						return true;
				} else if (data[index] == (byte) 0x8B) { // PRINT
					index++;
					Operand op = readOperand();
					// TODO check op type is string or number, or something good...
					appendText(op.value.toString());
					// TODO check data[index] == 0xFD
					index++;
				} else if (data[index] == (byte) 0x8C) { // SOUND
					index++;
					Operand op = readOperand();
					// TODO check op type is string.
					handled = true;
					callbacks.playSound(op.value.toString());
					// TODO check data[index] == 0xFD
					index++;
				} else if (data[index] == (byte) 0x8E) { // LET
					index++;
					processLet();
				} else if (data[index] == (byte) 0x95) { // MENU
					index++;
					Operand op = readStringOperand(); // allows empty menu
					// TODO check op type is string.
					callbacks.setMenu(op.value.toString());
					// TODO check data[index] == 0xFD
					index++;
				} else if (data[index] == (byte) 0x88) { // END
					index++;
				} else {
					System.out.println(buildStringFromOffset(index));
					System.exit(-1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(buildStringFromOffset(index));
			return true;
		}
		if (world.getGlobalScript() != this) {
			System.out.println("Executing global script...");
			boolean globalHandled = world.getGlobalScript().execute(world, loopCount, inputText, inputClick, callbacks);
			if (globalHandled)
				handled = true;
		} else if (inputText != null) {
			String input = inputText.toLowerCase();
			if (input.equals("n") || input.contains("north")) {
				handleMoveCommand(Scene.NORTH, "north");
			} else if (input.equals("e") || input.contains("east")) {
				handleMoveCommand(Scene.EAST, "east");
			} else if (input.equals("s") || input.contains("south")) {
				handleMoveCommand(Scene.SOUTH, "south");
			} else if (input.equals("w") || input.contains("west")) {
				handleMoveCommand(Scene.WEST, "west");
			} else if (input.startsWith("take ")) {
				handleTakeCommand(input.substring(5));
			} else if (input.startsWith("get ")) {
				handleTakeCommand(input.substring(4));
			} else if (input.startsWith("pick up ")) {
				handleTakeCommand(input.substring(8));
			} else if (input.startsWith("drop ")) {
				handleDropCommand(input.substring(5));
			} else if (input.startsWith("aim ")) {
				handleAimCommand(input.substring(4));
			} else if (input.startsWith("wear ")) {
				handleWearCommand(input.substring(5));
			} else if (input.startsWith("put on ")) {
				handleWearCommand(input.substring(7));
			} else if (input.startsWith("offer ")) {
				handleOfferCommand(input.substring(6));
			} else if (input.contains("look")) {
				handleLookCommand();
			} else if (input.contains("inventory")) {
				handleInventoryCommand();
			} else if (input.contains("status")) {
				handleStatusCommand();
			} else if (input.contains("rest") || input.equals("wait")) {
				handleRestCommand();
			} else if (callbacks.getOffer() != null && input.contains("accept")) {
				handleAcceptCommand();
			} else {
				Chr player = world.getPlayer();
				for (Weapon weapon : player.getWeapons()) {
					if (tryAttack(weapon, input)) {
						handleAttack(weapon);
						break;
					}
				}
			}
			// TODO: weapons, offer, etc...
		} else if (inputClick instanceof Obj) {
			Obj obj = (Obj) inputClick;
			if (obj.getType() != Obj.IMMOBILE_OBJECT) {
				takeObj(obj);
			} else {
				appendText(obj.getClickMessage());
			}
		}
		return handled;
	}

	private boolean tryAttack(Weapon weapon, String input) {
		return input.contains(weapon.getName().toLowerCase()) && input.contains(weapon.getOperativeVerb().toLowerCase());
	}

	private void handleAimCommand(String target) {
		boolean wasHandled = true;
		if (target.contains("head")) {
			callbacks.setAim(Chr.HEAD);
		} else if (target.contains("chest")) {
			callbacks.setAim(Chr.CHEST);
		} else if (target.contains("side")) {
			callbacks.setAim(Chr.SIDE);
		} else {
			wasHandled = false;
			appendText("Please aim for the head, chest, or side.");
		}
		if (wasHandled)
			handled = true;
		callbacks.setCommandWasQuick();
	}

	private void handleOfferCommand(String target) {
		Chr player = world.getPlayer();
		Chr enemy = callbacks.getMonster();
		if (enemy != null) {
			for (Obj o : player.getInventory()) {
				if (target.contains(o.getName().toLowerCase())) {
					// TODO
					if (true) {
						appendText("Your offer is rejected.");
					} else {
						appendText("Your offer is accepted.");
						appendText(enemy.getAcceptsOfferComment());
						world.move(o, enemy);
						world.move(enemy, world.getStorageScene());
					}
					break;
				}
			}
		}
	}
	
	private void handleAcceptCommand() {
		Obj offer = callbacks.getOffer();
		Chr chr = offer.getCurrentOwner();
		appendText("%s lays the %s on the ground and departs peacefully.",
			Engine.getNameWithDefiniteArticle(chr, true), offer.getName());
		world.move(offer, chr.getCurrentScene());
		world.move(chr, world.getStorageScene());
	}

	private void handleAttack(Weapon weapon) {
		Chr player = world.getPlayer();
		Chr enemy = callbacks.getMonster();
		if (enemy != null)
			callbacks.performAttack(player, enemy, weapon);
		else if (weapon.getType() == Obj.MAGICAL_OBJECT)
			appendText("There is nobody to cast a spell at.");
		else
			appendText("There is no one to fight.");
		handled = true;
	}

	private void handleInventoryCommand() {
		List<Obj> inv = world.getPlayer().getInventory();
		if (inv.size() == 0) {
			appendText("Your pack is empty.");
		} else {
			StringBuilder sb = new StringBuilder("Your pack contains ");
			appendObjNames(sb, inv, world.getPlayer().getArmor());
			appendText(sb.toString());
		}
	}
	
	private void handleLookCommand() {
		Scene playerScene = world.getPlayer().getCurrentScene();
		appendText(playerScene.getText());
		List<Obj> objs = playerScene.getObjs();
		for (Obj o : objs) {
			if (o.getType() != Obj.IMMOBILE_OBJECT) {
				StringBuilder sb = new StringBuilder("On the ground you see ");
				appendObjNames(sb, objs, new Obj[0]);
				appendText(sb.toString());
				break;
			}
		}
	}
	
	private void appendObjNames(StringBuilder sb, List<Obj> objs, Obj[] exclude) {
		objs_loop:
		for (int i = 0; i < objs.size(); i++) {
			Obj obj = objs.get(i);
			if (obj.getType() != Obj.IMMOBILE_OBJECT) {
				for (int j = 0; j < exclude.length; j++) {
					if (obj == exclude[j]) {
						continue objs_loop;
					}
				}
				if (!obj.isNamePlural())
					sb.append(TextUtils.prependIndefiniteArticle(obj.getName()));
				else
					sb.append(obj.getName());
				if (i == objs.size() - 1) {
					sb.append(".");
				} else if (i == objs.size() - 2) {
					if (objs.size() > 2)
						sb.append(",");
					sb.append(" and ");
				} else {
					sb.append(", ");
				}
			}
		}
	}

	private void handleStatusCommand() {
		Chr player = world.getPlayer();
		appendText("Character name: " + player.getName());
		appendText("Experience: " + player.getContext().getExperience());
		Obj[] armor = player.getArmor();
		if (armor[Chr.HEAD_ARMOR] != null)
			appendText("Head protection: " + armor[Chr.HEAD_ARMOR].getName());
		if (armor[Chr.BODY_ARMOR] != null)
			appendText("Body protection: " + armor[Chr.BODY_ARMOR].getName());
		int wealth = 0;
		for (Obj o : player.getInventory())
			wealth += o.getValue();
		appendText("Wealth: " + wealth);
		for (Obj o : player.getInventory()) {
			if (o.getNumberOfUses() > 0) {
				appendText(String.format("Your %s has %d uses left.", o.getName(), o.getNumberOfUses()));
			}
		}
		printPlayerCondition(player);
		callbacks.setCommandWasQuick();
	}
	
	public static String getPercentMessage(Chr chr, int cur, int bas) {
		Context context = chr.getContext();
		double percent = (double) context.getStatVariable(cur) / context.getStatVariable(bas);
		if (percent < 0.40) {
			return "very bad";
		} else if (percent < 0.55) {
			return "bad";
		} else if (percent < 0.7) {
			return "average";
		} else if (percent < 0.85) {
			return "good";
		} else {
			return "very good";
		}
	}

	private void printPlayerCondition(Chr player) {
		appendText("Your physical condition is " + getPercentMessage(player, Context.PHYS_HIT_CUR, Context.PHYS_HIT_BAS) + ".");
		appendText("Your spiritual condition is " + getPercentMessage(player, Context.SPIR_HIT_CUR, Context.SPIR_HIT_BAS) + ".");
	}

	private void handleRestCommand() {
		Chr player = world.getPlayer();
		Chr enemy = callbacks.getMonster();
		if (enemy != null) {
			appendText("This is no time to rest!");
			callbacks.setCommandWasQuick();
		} else {
			callbacks.regen();
			printPlayerCondition(player);
		}
	}

	private void takeObj(Obj obj) {
		Chr player = world.getPlayer();
		if (player.getInventory().size() >= player.getMaximumCarriedObjects()) {
			appendText("Your pack is full, you must drop something.");
		} else {
			world.move(obj, world.getPlayer());
			appendText("You now have the " + obj.getName() + ".");
			if (obj.getType() == Obj.HELMET) {
				if (player.getArmor()[Chr.HEAD_ARMOR] == null) {
					player.getArmor()[Chr.HEAD_ARMOR] = obj;
					appendText("You are now wearing the " + obj.getName() + ".");
				}
			} else if (obj.getType() == Obj.CHEST_ARMOR) {
				if (player.getArmor()[Chr.BODY_ARMOR] == null) {
					player.getArmor()[Chr.BODY_ARMOR] = obj;
					appendText("You are now wearing the " + obj.getName() + ".");
				}
			} else if (obj.getType() == Obj.SHIELD) {
				if (player.getArmor()[Chr.SHIELD_ARMOR] == null) {
					player.getArmor()[Chr.SHIELD_ARMOR] = obj;
					appendText("You are now wearing the " + obj.getName() + ".");
				}
			}
			appendText(obj.getClickMessage());
		}
	}
	
	private void handleTakeCommand(String target) {
		for (Obj o : world.getPlayer().getCurrentScene().getObjs()) {
			if (target.contains(o.getName().toLowerCase())) {
				if (o.getType() == Obj.IMMOBILE_OBJECT) {
					appendText("You can't move it.");
				} else {
					takeObj(o);
				}
				break;
			}
		}
	}

	private void handleDropCommand(String target) {
		for (Obj o : world.getPlayer().getInventory()) {
			if (target.contains(o.getName().toLowerCase())) {
				appendText("You no longer have the " + o.getName() + ".");
				world.move(o, world.getPlayer().getCurrentScene());
				break;
			}
		}
	}

	private void wearObj(Obj o, int pos) {
		Chr player = world.getPlayer();
		if (player.getArmor()[pos] == o) {
			appendText("You are already wearing the " + o.getName() + ".");
		} else {
			if (player.getArmor()[pos] != null) {
				appendText("You are no longer wearing the " + player.getArmor()[pos].getName() + ".");
			}
			player.getArmor()[pos] = o;
			appendText("You are now wearing the " + o.getName() + ".");
		}
	}
	
	private void handleWearCommand(String target) {
		Chr player = world.getPlayer();
		for (Obj o : player.getInventory()) {
			if (target.contains(o.getName().toLowerCase())) {
				if (o.getType() == Obj.HELMET) {
					wearObj(o, Chr.HEAD_ARMOR);
				} else if (o.getType() == Obj.CHEST_ARMOR) {
					wearObj(o, Chr.BODY_ARMOR);
				} else if (o.getType() == Obj.SHIELD) {
					wearObj(o, Chr.SHIELD_ARMOR);
				} else {
					appendText("You cannot wear that object.");
				}
				break;
			}
		}
	}

	private void handleMoveCommand(int dir, String dirName) {
		Chr player = world.getPlayer();
		Scene playerScene = player.getCurrentScene();
		String msg = playerScene.getDirMessage(dir);
		if (!playerScene.isDirBlocked(dir)) {
			int dx[] = new int[] { 0, 0, 1, -1 };
			int dy[] = new int[] { -1, 1, 0, 0 };
			int destX = playerScene.getWorldX() + dx[dir];
			int destY = playerScene.getWorldY() + dy[dir];
			Scene scene = world.getSceneAt(destX, destY);
			if (scene != null) {
				if (msg != null && msg.length() > 0) {
					appendText(msg);
				}
				world.move(player, scene);
				return;
			}
 		}
		if (msg != null && msg.length() > 0) {
			appendText(msg);
		} else {
			appendText("You can't go " + dirName + ".");
		}
	}
	
	private void indent(StringBuilder sb, int indentLevel) {
		for (int i = 0; i < indentLevel; i++)
			sb.append(' ');
	}

	private String buildStringFromOffset(int offset, int length) {
		StringBuilder sb = new StringBuilder();
		int indentLevel = 0;
		for (int i = offset; i - offset < length && i < data.length; i++) {
			if (data[i] == (byte) 0x80) {
				indent(sb, indentLevel);
				sb.append("IF{");
			} else if (data[i] == (byte) 0xA0) {
				sb.append("TEXT$");
			} else if (data[i] == (byte) 0xA1) {
				sb.append("CLICK$");
			} else if (data[i] == (byte) 0xC0) {
				sb.append("STORAGE@");
			} else if (data[i] == (byte) 0xC1) {
				sb.append("SCENE@");
			} else if (data[i] == (byte) 0xC2) {
				sb.append("PLAYER@");
			} else if (data[i] == (byte) 0xC3) {
				sb.append("MONSTER@");
			} else if (data[i] == (byte) 0xC4) {
				sb.append("RANDOMSCN@");
			} else if (data[i] == (byte) 0xC5) {
				sb.append("RANDOMCHR@");
			} else if (data[i] == (byte) 0xC6) {
				sb.append("RANDOMOBJ@");
			} else if (data[i] == (byte) 0x81) {
				sb.append("=");
			} else if (data[i] == (byte) 0x82) {
				sb.append("<");
			} else if (data[i] == (byte) 0x83) {
				sb.append(">");
			} else if (data[i] == (byte) 0x84) {
				sb.append("}AND{");
			} else if (data[i] == (byte) 0x85) {
				sb.append("}OR{");
			} else if (data[i] == (byte) 0x87) {
				indentLevel -= 2;
				indent(sb, indentLevel);
				sb.append("EXIT\n");
			} else if (data[i] == (byte) 0x88) {
				indentLevel -= 2;
				indent(sb, indentLevel);
				sb.append("END\n");
			} else if (data[i] == (byte) 0x89) {
				indent(sb, indentLevel);
				sb.append("MOVE{");
			} else if (data[i] == (byte) 0x8A) {
				sb.append("}TO{");
			} else if (data[i] == (byte) 0x8B) {
				indent(sb, indentLevel);
				sb.append("PRINT{");
			} else if (data[i] == (byte) 0x8C) {
				indent(sb, indentLevel);
				sb.append("SOUND{");
			} else if (data[i] == (byte) 0x8E) {
				indent(sb, indentLevel);
				sb.append("LET{");
			} else if (data[i] == (byte) 0x8F) {
				sb.append("+");
			} else if (data[i] == (byte) 0x90) {
				sb.append("-");
			} else if (data[i] == (byte) 0x91) {
				sb.append("*");
			} else if (data[i] == (byte) 0x92) {
				sb.append("/");
			} else if (data[i] == (byte) 0x93) {
				sb.append("==");
			} else if (data[i] == (byte) 0x94) {
				sb.append(">>");
			} else if (data[i] == (byte) 0x95) {
				indent(sb, indentLevel);
				sb.append("MENU{");
			} else if (data[i] == (byte) 0xFD) {
				sb.append("}\n");
			} else if (data[i] == (byte) 0xFE) {
				sb.append("}THEN\n");
				indentLevel += 2;
			} else if (data[i] == (byte) 0xFF) {
				i++;
				int value = data[i];
				if (value < 0) value += 256;
				value -= 1;
				sb.append((char) ('A' + (value / 9)));
				sb.append((value % 9) + 1);
				sb.append("#");
			} else if (data[i] == (byte) 0xB0) {
				// The number of scenes the player has visited, including repeated visits.
				sb.append("VISITS#");
			} else if (data[i] == (byte) 0xB1) {
				sb.append("RANDOM#"); // RANDOM# for Star Trek, but VISITS# for some other games?
			} else if (data[i] == (byte) 0xB5) {
				// A random number between 1 and 100.
				sb.append("RANDOM#");
			} else if (data[i] == (byte) 0xB2) {
				// The number of commands the player has given in the current scene.
				sb.append("LOOP#");
			} else if (data[i] == (byte) 0xB3) {
				// The number of characters killed.
				sb.append("VICTORY#");
			} else if (data[i] == (byte) 0xB4) {
				// ????
				sb.append("BADCOPY#");
			} else if (data[i] == (byte) 0xD0) {
				sb.append("PHYS.STR.BAS#");
			} else if (data[i] == (byte) 0xD1) {
				sb.append("PHYS.HIT.BAS#");
			} else if (data[i] == (byte) 0xD2) {
				sb.append("PHYS.ARM.BAS#");
			} else if (data[i] == (byte) 0xD3) {
				sb.append("PHYS.ACC.BAS#");
			} else if (data[i] == (byte) 0xD4) {
				sb.append("SPIR.STR.BAS#");
			} else if (data[i] == (byte) 0xD5) {
				sb.append("SPIR.HIT.BAS#");
			} else if (data[i] == (byte) 0xD6) {
				sb.append("SPIR.ARM.BAS#");
			} else if (data[i] == (byte) 0xD7) {
				sb.append("SPIR.ACC.BAS#");
			} else if (data[i] == (byte) 0xD8) {
				sb.append("PHYS.SPE.BAS#");
			} else if (data[i] == (byte) 0xE0) {
				sb.append("PHYS.STR.CUR#");
			} else if (data[i] == (byte) 0xE1) {
				sb.append("PHYS.HIT.CUR#");
			} else if (data[i] == (byte) 0xE2) {
				sb.append("PHYS.ARM.CUR#");
			} else if (data[i] == (byte) 0xE3) {
				sb.append("PHYS.ACC.CUR#");
			} else if (data[i] == (byte) 0xE4) {
				sb.append("SPIR.STR.CUR#");
			} else if (data[i] == (byte) 0xE5) {
				sb.append("SPIR.HIT.CUR#");
			} else if (data[i] == (byte) 0xE6) {
				sb.append("SPIR.ARM.CUR#");
			} else if (data[i] == (byte) 0xE7) {
				sb.append("SPIR.ACC.CUR#");
			} else if (data[i] == (byte) 0xE8) {
				sb.append("PHYS.SPE.CUR#");
			} else if (Character.isDefined(data[i])) {
				while (Character.isDefined(data[i]))
					sb.append((char) data[i++]);
				i--;
			} else {
				System.out.printf("What is!! %x\n", data[i]);
				//System.exit(-1);
			}
		}
		return sb.toString();
	}

	private String buildStringFromOffset(int offset) {
		return buildStringFromOffset(offset, data.length - offset);
	}
	
	public String toString() {
		String s = buildStringFromOffset(12);
		StringBuilder sb = new StringBuilder("  0: ");
		int lineno = 1;
		for (int i = 0; i < s.length(); i++) {
			sb.append(s.charAt(i));
			if (s.charAt(i) == '\n')
				sb.append(String.format("%3d: ", lineno++));
		} 
		return sb.toString();
	}
	
	private int indexToLine(int index) {
		int loc = 0;
		for (char c : buildStringFromOffset(12, index).toCharArray())
			if (c == '\n')
				loc++;
		return loc - 1;
	}
	
	public int countLines() {
		int loc = 0;
		for (char c : toString().toCharArray())
			if (c == '\n')
				loc++;
		return loc;
	}
}
