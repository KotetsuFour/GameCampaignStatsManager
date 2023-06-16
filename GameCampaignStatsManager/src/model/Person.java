package model;

import java.util.ArrayList;

public class Person {

	public static int currentId = 0;
	private int id;
	private String name;
	private String rank;
	private int score; //kills, experience, however you want to conceptualize it
	private boolean alive;
	private boolean participating;
	private boolean safe;
	private ArrayList<int[]> battleData;
	private ArrayList<Integer> promotions; //each int is last battle id before the promotion
	private int joinTime; //Person joined their unit after this many battles passed
	public static final int DIED = -1;
	public static final int UNDETERMINED = -2;
	
	
	public Person() {
		this.id = currentId;
		currentId++;
		this.alive = true;
		battleData = new ArrayList<>();
		promotions = new ArrayList<>();
	}
	public Person(int battlesPassed) {
		this();
		this.joinTime = battlesPassed;
	}
	/**
	 * Constructor for loading from file, not for initialization
	 * @param id
	 * @param name
	 * @param rank
	 * @param score
	 * @param alive
	 */
	public Person(int id, String name, String rank, int score, boolean alive, int join) {
		this.id = id;
		this.name = name;
		this.rank = rank;
		this.score = score;
		this.alive = alive;
		this.joinTime = join;
		battleData = new ArrayList<>();
		promotions = new ArrayList<>();
	}
	public int getId() {
		return id;
	}
	public void resetId() {
		this.id = currentId;
		currentId++;
	}
	public String getRank() {
		return rank;
	}
	public int getScore() {
		return score;
	}
	public boolean isAlive() {
		return alive;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public int getJoinTime() {
		return joinTime;
	}
	public ArrayList<Integer> getPromotions() {
		return promotions;
	}
	public void kill() {
		this.alive = false;
		battleData.get(battleData.size() - 1)[1] = DIED;
	}
	public void givePoints(int points) {
		score += points;
		battleData.get(battleData.size() - 1)[1] = points;
	}
	public void setParticipating(boolean participating) {
		if (!isAlive()) {
			participating = false;
		}
		this.participating = participating;
	}
	public boolean isParticipating() {
		return participating;
	}
	public void setSafe(boolean safe) {
		this.safe = safe;
	}
	public boolean isSafe() {
		return safe;
	}
	public void addBattle(BattleStats stats) {
		int[] b = {stats.getId(), UNDETERMINED};
		battleData.add(b);
	}
	/**
	 * For loading from file
	 * @param b
	 */
	public void addBattle(int[] b) {
		battleData.add(b);
	}
	public void promote(String rank, int battleId) {
		setRank(rank);
		promotions.add(battleId);
	}
	public int getCurrentBattleScore() {
		return battleData.get(battleData.size() - 1)[1];
	}
	public ArrayList<int[]> getBattles() {
		return battleData;
	}
	
	public void print() {
		if (rank != null) {
			System.out.print(rank + " ");
		}
		System.out.print(id);
		if (name != null) {
			System.out.print(" (" + name + ")");
		}
		System.out.println();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (rank != null) {
			sb.append(rank + " ");
		}
		if (name == null) {
			sb.append(id);
		} else {
			sb.append(name + " (" + id + ")");
		}
		return sb.toString();
	}
}
