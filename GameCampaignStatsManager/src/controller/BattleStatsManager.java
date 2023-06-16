package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import model.BattleStats;
import model.Person;
import model.UnitType;

import java.util.Random;

public class BattleStatsManager {

	public static final Random rng = new Random();
	public static ArrayList<UnitType> typesList;
	public static UnitType trackedUnit;
	public static Object selectedObject;
	public static ArrayList<BattleStats> battles;

	public BattleStatsManager() {
		typesList = new ArrayList<>();
		battles = new ArrayList<>();
	}
	
	public ArrayList<UnitType> getTypesList() {
		return typesList;
	}
	public void setTrackedUnit(UnitType unit) {
		trackedUnit = unit;
		typesList = null;
		Person.currentId = 0;
		trackedUnit.resetIds();
	}
	public UnitType getTrackedUnit() {
		return trackedUnit;
	}
	
	//This method takes all of the combatants, not just the survivors. It does not take into
	//account the fact that the survivors would probably receive more points than the dead
	public void distributePoints(ArrayList<Person> people, int points) {
		if (points >= people.size()) {
			//First, BOGO sort to randomize the list of people so that our assignments are random
			//Otherwise, we would favor the people at the beginning of the list
			for (int q = 0; q < people.size(); q++) {
				int idx = rng.nextInt(people.size());
				Person temp = people.get(idx);
				people.set(idx, people.get(q));
				people.set(q, temp);
			}
			
			int avg = points / people.size();
			avg = (avg * 2) + 1; //Parameter for randomization
			int idx = 0;
			for (int q = 0; q < people.size() && points > 0; q++) {
				int give = Math.min(rng.nextInt(avg), points);
				registerKills(people.get(q), give);
				points -= give;
				idx++;
			}
			//Any leftover points could be given away to someone, but
			// I'll probably just let them go to oblivion like this
			for (int q = idx; q < people.size(); q++) {
				registerKills(people.get(q), 0);
			}
		} else {
			for (int q = 0; q < points; q++) {
				//Probably not the best way to do this, but it's fine
				people.get(rng.nextInt(people.size())).givePoints(1);
			}
		}
	}
	
//	//This method assumes that all of the people in the list are the survivors of the battle
//	public void assignPoints(ArrayList<Person> people, ArrayList<Integer> points) {
//		//First, BOGO sort to randomize the list of people so that our assignments are random
//		//Otherwise, we would favor the people at the beginning of the list
//		for (int q = 0; q < people.size(); q++) {
//			int idx1 = rng.nextInt(people.size());
//			int idx2 = rng.nextInt(people.size());
//			Person temp = people.get(idx1);
//			people.set(idx1, people.get(idx2));
//			people.set(idx2, temp);
//		}
//		
//		//Then give the list of points in order
//		for (int q = 0; q < points.size() && q < people.size(); q++) {
//			people.get(q).givePoints(points.get(q));
//		}
//	}
	
	//This method assumes that all safe combatants have already been taken out of the people list
	public ArrayList<Person> decideCasualties(ArrayList<Person> people, int deaths) {
		//BOGO sort to randomize the list of people so that our assignments are random
		for (int q = 0; q < people.size(); q++) {
			int idx = rng.nextInt(people.size());
			Person temp = people.get(idx);
			people.set(idx, people.get(q));
			people.set(q, temp);
		}
		
		//Kill the first "deaths" amount of people in the randomly sorted list, then
		//return a list of the survivors
		deaths = Math.min(deaths, people.size());
		for (int q = 0; q < deaths; q++) {
			registerDeath(people.get(q));
		}
//		ArrayList<Person> ret = new ArrayList<>();
//		for (int q = deaths; q < people.size(); q++) {
//			ret.add(people.get(q));
//		}
//		return ret;
		return people;
	}
	
	public void renameSelectedObject(String name) {
		if (selectedObject instanceof UnitType) {
			((UnitType)selectedObject).setName(name);
		} else if (selectedObject instanceof Person) {
			((Person)selectedObject).setName(name);
		}
		selectedObject = null;
	}
	
	public void removeAllParticipants() {
		trackedUnit.setParticipating(false);
		selectedObject = null;
	}
	
	public void startBattle(String name) {
		BattleStats b = new BattleStats(battles.size(), name);
		battles.add(b);
		ArrayList<Person> combatants = getPeopleToConsider();
		b.addAllies(combatants.size());
		for (int q = 0; q < combatants.size(); q++) {
			combatants.get(q).addBattle(b);
		}
	}
	public ArrayList<Person> getPeopleToConsider() {
		ArrayList<Person> ret = trackedUnit.getAllPeople();
		for (int q = 0; q < ret.size(); q++) {
			Person p = ret.get(q);
			if (!p.isParticipating() || !p.isAlive()) {
				ret.remove(q);
				q--;
			}
		}
		return ret;
	}
	public ArrayList<Person> getNamedPeopleToCheck() {
		ArrayList<Person> ret = getPeopleToConsider();
		for (int q = 0; q < ret.size(); q++) {
			Person p = ret.get(q);
			if (p.getName() == null || p.getCurrentBattleScore() != Person.UNDETERMINED) {
				ret.remove(q);
				q--;
			}
		}
		return ret;
	}
	public void registerDeath(Person p) {
		p.kill();
		battles.get(battles.size() - 1).addDeaths(1);
	}
	public void registerKills(Person p, int kills) {
		p.givePoints(kills);
		battles.get(battles.size() - 1).addKills(kills);
	}
	
	public void handleBattle(int kills, int deaths) {
		ArrayList<Person> list = getPeopleToConsider();
		for (int q = 0; q < list.size(); q++) {
			Person p = list.get(q);
			if (p.getCurrentBattleScore() != Person.UNDETERMINED) {
				list.remove(q);
				q--;
			}
		}
		distributePoints(list, kills);
		for (int q = 0; q < list.size(); q++) {
			Person p = list.get(q);
			if (p.isSafe()) {
				list.remove(q);
				q--;
			}
		}
		decideCasualties(list, deaths);
		
		trackedUnit.setParticipating(false);
		trackedUnit.setAllUnsafe();
	}
	
	public String getUnitStats() {
		UnitType unit = (UnitType)selectedObject;
		
		StringBuilder sb = new StringBuilder("<html>" + unit.getName() + ":<br/><html><br/>");
		sb.append(unit.getNumbers());
		
		return sb.toString();
	}
	
	public void printStats() throws IOException {
		StatsManagerIO.printStats(trackedUnit, battles);
	}
	public void saveStats() throws IOException {
		StatsManagerIO.saveStats(battles, trackedUnit);
	}
	public void loadStats(File file) throws FileNotFoundException {
		StatsManagerIO.loadStats(file);
		typesList = null;
	}
	public void giveNames(File file, UnitType unit, int num) throws FileNotFoundException {
		ArrayList<String> names = StatsManagerIO.getNamesFromFile(file);
		giveNamesHelper(names, unit, num);
	}
	private void giveNamesHelper(ArrayList<String> names, UnitType unit, int num) {
		for (int q = 0; q < unit.getMembers().size(); q++) {
			Person p = unit.getMembers().get(q);
			if (p.getName() != null) {
				continue;
			}
			int name = rng.nextInt(num);
			if (name >= names.size()) {
				p.setName("#" + name);
			} else {
				p.setName(names.get(name));
			}
		}
		for (int q = 0; q < unit.getSubUnits().size(); q++) {
			giveNamesHelper(names, unit.getSubUnits().get(q), num);
		}
	}
}
