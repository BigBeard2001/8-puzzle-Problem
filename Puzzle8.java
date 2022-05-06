import java.io.*;
import java.util.*;

public class Puzzle8 {
	public static void main(String[] args) throws Exception {
		int[][] standard = new int[][] { { 2, 2 }, { 0, 0 }, { 0, 1 }, { 0, 2 }, { 1, 0 }, { 1, 1 }, { 1, 2 }, { 2, 0 },
			{ 2, 1 }};
		State initial = ReadBoard(args[0]);
		initial.GenerateState(null, standard);
		State finalAnswer = Progress(initial, standard);
		Stack<State> box = new Stack<State>();
		while (finalAnswer.prev != null) {
			box.push(finalAnswer);
			finalAnswer = finalAnswer.prev;
		}
		box.push(finalAnswer);
		WriteFile(box, args[1]);
	}

	public static State Progress(State initial, int[][] standard) {
		int[][] goal = new int[][]{{1,2,3},{4,5,6},{7,8,0}};
		Queue<State> priorityQueue = new PriorityQueue<State>(idComparator);
		ArrayList<State> moveQueue = new ArrayList<State>();
		priorityQueue.offer(initial);
		while (priorityQueue.size() != 0) {
			State present = priorityQueue.poll();
			if (Arrays.deepEquals(present.board, goal)) {
				return present;
			}
			moveQueue.add(present);
			ArrayList<State> list = neighbor(present);
			
			for (State state:list) {
				state.GenerateState(present, standard);
				if (InOpenlist(priorityQueue, state.board)) {
					for (int i = 0; i < priorityQueue.size(); i++) {
						State t = priorityQueue.poll();
						priorityQueue.offer(t);
						if (Arrays.deepEquals(t.board, state.board) && t.moves > state.moves) {
							priorityQueue.remove(t);
							priorityQueue.offer(state);
						}
					}
				}
				if (InCloselist(moveQueue, state.board)) {
					for (State i:moveQueue) {
						if (Arrays.deepEquals(i.board, state.board) && i.moves > state.moves) {
							moveQueue.remove(i);
							priorityQueue.offer(state);
						}
					}
				}
				if (!InOpenlist(priorityQueue, state.board) && !InCloselist(moveQueue, state.board)) {
					priorityQueue.offer(state);
				}
			}
		}
		return null;
	}

	public static boolean InCloselist(ArrayList<State> moveQueue, int[][] board) {
		boolean flag = false;
		for (State i:moveQueue) {
			if (Arrays.deepEquals(i.board, board)) {
				return true;
			}
		}
		return flag;
	}
	
	public static boolean InOpenlist(Queue<State> priorityQueue, int[][] board) {
		boolean flag = false;
		for (State i:priorityQueue) {
			if (Arrays.deepEquals(i.board, board)) {
				return true;
			}
		}
		return flag;
	}
	
	public static Comparator<State> idComparator = new Comparator<State>() {
		@Override
		public int compare(State s1, State s2) {
			return (int) (s1.MPriority - s2.MPriority);
		}
	};

	public static State MoveUp(State present, int row, int col) {
		int[][] neighbor = copy(present.board);
		int temp = neighbor[row][col];
		neighbor[row][col] = neighbor[row - 1][col];
		neighbor[row - 1][col] = temp;
		return (new State(neighbor));
	}
	
	public static State MoveDown(State present, int row, int col) {
		int[][] neighbor = copy(present.board);
		int temp = neighbor[row][col];
		neighbor[row][col] = neighbor[row + 1][col];
		neighbor[row + 1][col] = temp;
		return (new State(neighbor));
	}
	
	public static State MoveLeft(State present, int row, int col) {
		int[][] neighbor = copy(present.board);
		int temp = neighbor[row][col];
		neighbor[row][col] = neighbor[row][col - 1];
		neighbor[row][col - 1] = temp;
		return (new State(neighbor));
	}
	
	public static State MoveRight(State present, int row, int col) {
		int[][] neighbor = copy(present.board);
		int temp = neighbor[row][col];
		neighbor[row][col] = neighbor[row][col + 1];
		neighbor[row][col + 1] = temp;
		return (new State(neighbor));
	}
	
	public static ArrayList<State> neighbor(State present) {
		ArrayList<State> list = new ArrayList<State>();
		int[][] board = present.board;
		int row = 0;
		int col = 0;
		Outer: for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board[i][j] == 0) {
					row = i;
					col = j;
					break Outer;
				}
			}
		}
		
		if (row > 0) {
			list.add(MoveUp(present, row, col));
		}
		if (row < 2) {
			list.add(MoveDown(present, row, col));
		}
		if (col > 0) {
			list.add(MoveLeft(present, row, col));
		}
		if (col < 2) {
			list.add(MoveRight(present, row, col));
		}
		return list;
	}

	public static int[][] copy(int[][] board) {
		int[][] neighbor = new int[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				neighbor[i][j] = board[i][j];
			}
		}
		return neighbor;
	}

	public static void WriteFile(Stack<State> box, String path) throws Exception {
		FileWriter fwriter = new FileWriter(path, false);
		BufferedWriter bwriter = new BufferedWriter(fwriter);
		while (!box.isEmpty()) {
			int[][] board = box.pop().board;
			for (int i = 0; i < 3; i++) {
				String line = board[i][0] + " " + board[i][1] + " " + board[i][2];
				bwriter.write(line);
				if (i !=2 || !box.isEmpty()) {
					bwriter.newLine();
				}
			}
			if (!box.isEmpty()) {
				bwriter.newLine();
			}
		}
		bwriter.flush();
		bwriter.close();
		return;
	}

	public static State ReadBoard(String path) throws Exception {
		int[][] board = new int[3][3];
		File file = new File(path);
		try {
			int i = 0;
			String line = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((line = reader.readLine()) != null) {
				String[] vStrs = line.split(" ");
				int j = 0;
				for (String str : vStrs) {
					board[i][j] = Integer.parseInt(str);
					j++;
				}
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		State initial = new State(board);
		return initial;
	}
}

class State {
	int[][] board;
	int moves;
	State prev;
	int MDistance = 0;
	int MPriority = 0;

	public State(int[][] board) {
		this.board = board;
	}

	public void GenerateState(State prev, int[][] standard) {
		this.prev = prev;

		if (prev != null) {
			this.moves = prev.moves + 1;
		} else {
			this.moves = 0;
		}

		for (int i = 0; i < 3; i++) { // i for row, j for column.
			for (int j = 0; j < 3; j++) {
				int num = this.board[i][j];
				if (num != 0) {
					int[] standardPosition = standard[num];
					int subDis = Math.abs(standardPosition[0] - i) + Math.abs(standardPosition[1] - j);
					this.MDistance = this.MDistance + subDis;
				}
			}
		}
		this.MPriority = this.moves + this.MDistance;
	}
}
