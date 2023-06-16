package model;

public class BattleStats {

	private int id;
	private String name;
	private int kills;
	private int deaths;
	private int totalAlliedParticipants;
	
	public BattleStats(int id, String name) {
		this.id = id;
		this.name = name;
	}
	/**
	 * Constructor for loading from file, not for initializing
	 * @param id
	 * @param name
	 * @param kills
	 * @param deaths
	 * @param totalAlliedParticipants
	 */
	public BattleStats(int id, String name, int kills, int deaths, int totalAlliedParticipants) {
		this.id = id;
		this.name = name;
		this.kills = kills;
		this.deaths = deaths;
		this.totalAlliedParticipants = totalAlliedParticipants;
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getKills() {
		return kills;
	}
	public int getDeaths() {
		return deaths;
	}
	public int getTotalAllies() {
		return totalAlliedParticipants;
	}
	
	public void addKills(int num) {
		kills += num;
	}
	public void addDeaths(int num) {
		deaths += num;
	}
	public void addAllies(int num) {
		totalAlliedParticipants += num;
	}
}
