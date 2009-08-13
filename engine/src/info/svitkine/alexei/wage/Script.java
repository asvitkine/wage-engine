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
			for (Chr chr : world.getPlayer().getCurrentScene().getChrs()) {
				if (!chr.isPlayerCharacter()) {
					result = new Operand(chr, Operand.CHR);
					break;
				}
			}
			if (result == null) {
				result = new Operand(null, Operand.CHR);
			}
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
			result = new Operand(world.getPlayer().getVisits(), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB1) {
			// RANDOM# for Star Trek, but VISITS# for some other games?
			result = new Operand(1 + (int) (Math.random()*100), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB5) { // RANDOM#
			// A random number between 1 and 100.
			result = new Operand(1 + (int) (Math.random()*100), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB2) { // LOOP#
			result = new Operand(loopCount, Operand.NUMBER);
		} else if (data[index] == (byte) 0xB3) { // VICTORY#
			result = new Operand(world.getPlayer().getKills(), Operand.NUMBER);
		} else if (data[index] == (byte) 0xB4) { // BADCOPY#
			result = new Operand(0, Operand.NUMBER); // ????
		} else if (data[index] == (byte) 0xFF) {
			// user variable
			int value = data[++index];
			if (value < 0) value += 256;
			// TODO: Verify that we're using the right index.
			result = new Operand(world.getPlayerContext().getUserVariable(value), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD0) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_STR_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD1) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_HIT_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD2) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_ARM_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD3) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_ACC_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD4) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_STR_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD5) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_HIT_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD6) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_ARM_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD7) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_ACC_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xD8) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_SPE_BAS), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE0) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_STR_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE1) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_STR_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE2) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_ARM_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE3) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_ACC_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE4) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_STR_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE5) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_HIT_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE6) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_ARM_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE7) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.SPIR_ACC_CUR), Operand.NUMBER);
		} else if (data[index] == (byte) 0xE8) {
			result = new Operand(world.getPlayerContext().getPlayerVariable(Context.PHYS_SPE_CUR), Operand.NUMBER);
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
					evalResult = (c.getCurrentScene() != s);
				}
			});
			evaluatePair(handlers, lhs, rhs);
			result = (Boolean) evalResult;
		} else if (op.equals("==") || op.equals(">>")) {
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
		if (result == null) {
			System.err.println("OMG UNHANDLED CASE FIXME (op is " + op + ")");
			result = false;
		}
		return result;
	}

	private void skipBlock() {
		int nesting = 1;
		while (index < data.length) {
			if (data[index] == (byte) 0x80) { // IF
				nesting++;
			} else if (data[index] == (byte) 0x88 || data[index] == (byte) 0x87) { // END or EXIT
				nesting--;
				if (nesting == 0) {
					index++;
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
		public void appendText(String text);
		public void playSound(String sound);
		public void setMenu(String menuData);
	}
	
	private void processIf() {
		System.out.println("I love conditionals!");
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
			context.setPlayerVariable(Context.PHYS_STR_BAS, value);
		} else if (data[index] == (byte) 0xD1) {
			context.setPlayerVariable(Context.PHYS_HIT_BAS, value);
		} else if (data[index] == (byte) 0xD2) {
			context.setPlayerVariable(Context.PHYS_ARM_BAS, value);
		} else if (data[index] == (byte) 0xD3) {
			context.setPlayerVariable(Context.PHYS_ACC_BAS, value);
		} else if (data[index] == (byte) 0xD4) {
			context.setPlayerVariable(Context.SPIR_STR_BAS, value);
		} else if (data[index] == (byte) 0xD5) {
			context.setPlayerVariable(Context.SPIR_HIT_BAS, value);
		} else if (data[index] == (byte) 0xD6) {
			context.setPlayerVariable(Context.SPIR_ARM_BAS, value);
		} else if (data[index] == (byte) 0xD7) {
			context.setPlayerVariable(Context.SPIR_ACC_BAS, value);
		} else if (data[index] == (byte) 0xD8) {
			context.setPlayerVariable(Context.PHYS_SPE_BAS, value);
		} else if (data[index] == (byte) 0xE0) {
			context.setPlayerVariable(Context.PHYS_STR_CUR, value);
		} else if (data[index] == (byte) 0xE1) {
			context.setPlayerVariable(Context.PHYS_STR_CUR, value);
		} else if (data[index] == (byte) 0xE2) {
			context.setPlayerVariable(Context.PHYS_ARM_CUR, value);
		} else if (data[index] == (byte) 0xE3) {
			context.setPlayerVariable(Context.PHYS_ACC_CUR, value);
		} else if (data[index] == (byte) 0xE4) {
			context.setPlayerVariable(Context.SPIR_STR_CUR, value);
		} else if (data[index] == (byte) 0xE5) {
			context.setPlayerVariable(Context.SPIR_HIT_CUR, value);
		} else if (data[index] == (byte) 0xE6) {
			context.setPlayerVariable(Context.SPIR_ARM_CUR, value);
		} else if (data[index] == (byte) 0xE7) {
			context.setPlayerVariable(Context.SPIR_ACC_CUR, value);
		} else if (data[index] == (byte) 0xE8) {
			context.setPlayerVariable(Context.PHYS_SPE_CUR, value);
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
	
	public void execute(World world, int loopCount,
			String inputText, Object inputClick,
			Callbacks callbacks)
	{
		this.world = world;
		this.loopCount = loopCount;
		this.inputText = inputText;
		this.inputClick = inputClick;
		this.callbacks = callbacks;
		try {
			index = 12;
			while (index < data.length) {
				if (data[index] == (byte) 0x80) { // IF{
					index++;
					processIf();
				} else if (data[index] == (byte) 0x87) { // EXIT
					return;
				} else if (data[index] == (byte) 0x89) { // MOVE
					index++;
					Scene currentScene = world.getPlayer().getCurrentScene();
					processMove();
					if (world.getPlayer().getCurrentScene() != currentScene)
						return;
				} else if (data[index] == (byte) 0x8B) { // PRINT
					index++;
					Operand op = readOperand();
					// TODO check op type is string or number, or something good...
					callbacks.appendText(op.value.toString());
					// TODO check data[index] == 0xFD
					index++;
				} else if (data[index] == (byte) 0x8C) { // SOUND
					index++;
					Operand op = readOperand();
					// TODO check op type is string.
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
			return;
		}
		if (world.getGlobalScript() != this) {
			System.out.println("Executing global script...");
			world.getGlobalScript().execute(world, loopCount, inputText, inputClick, callbacks);
		} else if (inputText != null) {
			String input = inputText.toLowerCase();
			if (input.equals("n") || input.contains("north")) {
				handleMoveCommand(Scene.NORTH, "north");
			} else if (input.equals("e") || input.contains("east")) {
				handleMoveCommand(Scene.EAST, "east");
			} else if (input.equals("s") || inputText.toLowerCase().contains("south")) {
				handleMoveCommand(Scene.SOUTH, "south");
			} else if (input.equals("w") || inputText.toLowerCase().contains("west")) {
				handleMoveCommand(Scene.WEST, "west");
			} else if (input.startsWith("take ")) {
				handleTakeCommand(input.substring(5));
			} else if (input.startsWith("get ")) {
				handleTakeCommand(input.substring(4));
			} else if (input.startsWith("pick up ")) {
				handleTakeCommand(input.substring(8));
			} else if (input.startsWith("drop ")) {
				handleDropCommand(input.substring(5));
			} else if (input.contains("look")) {
				handleLookCommand();
			} else if (input.contains("inventory")) {
				handleInventoryCommand();
			} else if (input.contains("status")) {
				handleStatusCommand();
			} else if (input.contains("rest")) {
				handleRestCommand();
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
				world.move(obj, world.getPlayer());
				callbacks.appendText("You now have the " + obj.getName() + ".");
			}
			if (obj.getClickMessage() != null) {
				callbacks.appendText(obj.getClickMessage());
			}
		}
	}

	private boolean tryAttack(Weapon weapon, String input) {
		return input.contains(weapon.getName().toLowerCase()) && input.contains(weapon.getOperativeVerb().toLowerCase());
	}

	private void handleAttack(Weapon weapon) { // TODO:
		if (weapon.getType() == Obj.MAGICAL_OBJECT)
			callbacks.appendText("There is nobody to cast a spell at.");
		else
			callbacks.appendText("There is no one to fight.");
	}

	private void handleInventoryCommand() {
		List<Obj> inv = world.getPlayer().getInventory();
		if (inv.size() == 0) {
			callbacks.appendText("Your pack is empty.");
		} else {
			StringBuilder sb = new StringBuilder("Your pack contains ");
			appendObjNames(sb, inv);
			callbacks.appendText(sb.toString());
		}
	}
	
	private void handleLookCommand() {
		Scene playerScene = world.getPlayer().getCurrentScene();
		callbacks.appendText(playerScene.getText());
		List<Obj> objs = playerScene.getObjs();
		for (Obj o : objs) {
			if (o.getType() != Obj.IMMOBILE_OBJECT) {
				StringBuilder sb = new StringBuilder("On the ground you see ");
				appendObjNames(sb, objs);
				callbacks.appendText(sb.toString());
				break;
			}
		}
	}
	
	private void appendObjNames(StringBuilder sb, List<Obj> objs) {
		for (int i = 0; i < objs.size(); i++) {
			Obj obj = objs.get(i);
			if (obj.getType() != Obj.IMMOBILE_OBJECT) {
				if (!obj.isNamePlural())
					sb.append("a ");
				sb.append(obj.getName());
				if (i == objs.size() - 1)
					sb.append(".");
				else if (i == objs.size() - 2)
					sb.append(" and ");
				else
					sb.append(", ");
			}
		}
	}

	private void handleStatusCommand() {
		Chr player = world.getPlayer();
		callbacks.appendText("Character name: " + player.getName());
		callbacks.appendText("Experience: " + player.getKills());
		int wealth = 0;
		for (Obj o : player.getInventory())
			wealth += o.getValue();
		callbacks.appendText("Wealth: " + wealth);
		String physCond = "very good"; // TODO
		callbacks.appendText("Your physical condition is " + physCond + ".");
		String spirCond = "very good"; // TODO
		callbacks.appendText("Your spiritual condition is " + spirCond + ".");
	}

	private void handleRestCommand() {
	}

	private void handleTakeCommand(String target) {
		for (Obj o : world.getPlayer().getCurrentScene().getObjs()) {
			if (target.contains(o.getName().toLowerCase())) {
				if (o.getType() == Obj.IMMOBILE_OBJECT) {
					callbacks.appendText("You can't move it.");
				} else {
					// TODO: What about limit # of objs and such.
					callbacks.appendText("You now have the " + o.getName() + ".");
					callbacks.appendText(o.getClickMessage());
					world.move(o, world.getPlayer());
				}
				break;
			}
		}
	}
	
	private void handleDropCommand(String target) {
		for (Obj o : world.getPlayer().getInventory()) {
			if (target.contains(o.getName().toLowerCase())) {
				callbacks.appendText("You no longer have the " + o.getName() + ".");
				world.move(o, world.getPlayer().getCurrentScene());
				break;
			}
		}
	}
	
	private void handleMoveCommand(int dir, String dirName) {
		Chr player = world.getPlayer();
		Scene playerScene = player.getCurrentScene();
		String msg = playerScene.getDirMessage(dir);
		if (playerScene.isDirBlocked(dir)) {
			if (msg != null && msg.length() > 0) {
				callbacks.appendText(msg);
			} else {
				callbacks.appendText("You can't go " + dirName + ".");
			}
		} else {
			if (msg != null && msg.length() > 0) {
				callbacks.appendText(msg);
			}
			int dx[] = new int[] { 0, 0, 1, -1 };
			int dy[] = new int[] { -1, 1, 0, 0 };
			int destX = playerScene.getWorldX() + dx[dir];
			int destY = playerScene.getWorldY() + dy[dir];
			for (Scene scene : world.getScenes().values()) {
				if (scene != world.getStorageScene() && scene.getWorldX() == destX && scene.getWorldY() == destY) {
					world.move(player, scene);
					break;
				}
			}
 		}
	}
	
	private void indent(StringBuilder sb, int indentLevel) {
		for (int i = 0; i < indentLevel; i++)
			sb.append(' ');
	}

	private String buildStringFromOffset(int offset) {
		StringBuilder sb = new StringBuilder();
		int indentLevel = 0;
		for (int i = offset; i < data.length; i++) {
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
	
	public String toString() {
		return buildStringFromOffset(12);
	}
}
