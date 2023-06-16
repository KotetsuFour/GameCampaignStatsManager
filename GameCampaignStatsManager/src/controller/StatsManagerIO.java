package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import model.BattleStats;
import model.Person;
import model.UnitType;

public class StatsManagerIO {

	//Print stats in a nice, neat format
	public static void printStats(UnitType unit, ArrayList<BattleStats> battles)
		throws IOException {
		String fileNameAppend = unit.getName().replace(" ", "_");
		PrintStream print = new PrintStream(new File("output/" + fileNameAppend + "_data.txt"));
		int lineLength = 22 + (11 * battles.size());
		
		print.print(String.format("|%21.21s", unit.getName()));
		for (int q = 0; q < battles.size(); q++) {
			print.print(String.format("|%10.10s", battles.get(q).getName()));
		}
		print.println();
		for (int q = 0; q < lineLength; q++) {
			print.print("-");
		}
		print.println();
		
		ArrayList<Person> people = unit.getAllPeople();
		for (Person p : people) {
			if (p.getRank() == null) {
				print.print(String.format("|%10.10s", "---"));
			} else {
				print.print(String.format("|%10.10s", p.getRank()));
			}
			if (p.getName() == null) {
				print.print(String.format("|%10.10s", "---"));
			} else {
				print.print(String.format("|%10.10s", p.getName()));
			}
			int idx = 0;
			for (BattleStats b : battles) {
				if (idx < p.getBattles().size()
						&& b.getId() == p.getBattles().get(idx)[0]) {
					if (p.getBattles().get(idx)[1] == Person.DIED) {
						print.print(String.format("|%10.10s", "DIED"));
					} else {
						print.print(String.format("|%10d", p.getBattles().get(idx)[1]));
					}
					idx++;
				} else {
					print.print(String.format("|%10.10s", ""));
				}
			}
			print.println();
			for (int q = 0; q < lineLength; q++) {
				print.print("-");
			}
			print.println();
		}
		
		print.close();
	}
	
	public static void saveStats(ArrayList<BattleStats> battles, UnitType unit)
			throws IOException {
		String fileNameAppend = unit.getName().replace(" ", "_");
		PrintStream print = new PrintStream(new File("saves/" + fileNameAppend + "_save.txt"));
		
		print.println(Person.currentId + ",");
		print.println(UnitType.currentId + ",");
		for (int q = 0; q < battles.size(); q++) {
			BattleStats b = battles.get(q);
			print.println(String.format("B,%d,%s,%d,%d,%d", b.getId(), b.getName(),
					b.getKills(), b.getDeaths(), b.getTotalAllies()));
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (int q = 0; q < unit.getMembers().size(); q++) {
			Person p = unit.getMembers().get(q);
			sb.append(String.format(",s,%d", p.getId()));
			print.print(String.format("S,%d,%s,%s,%d,%b,%d", p.getId(), p.getName(),
					p.getRank(), p.getScore(), p.isAlive(), p.getJoinTime()));
			for (int w = 0; w < p.getBattles().size(); w++) {
				int[] b = p.getBattles().get(w);
				print.print(String.format(",%d,%d", b[0], b[1]));
			}
			print.print(",p");
			for (int w = 0; w < p.getPromotions().size(); w++) {
				int prom = p.getPromotions().get(w);
				print.print(String.format(",%d", prom));
			}
			print.println();
		}
		for (int q = 0; q < unit.getSubUnits().size(); q++) {
			UnitType u = unit.getSubUnits().get(q);
			sb.append(String.format(",u,%d", u.getId()));
			saveHelper(print, u);
		}
		
		print.print(String.format("T,%d,%s,%s,%d", unit.getId(), unit.getName(),
				unit.getOfficerTitle(), unit.getLeadingSubUnit()));
		print.println(sb.toString());
		
		print.close();
	}
	private static void saveHelper(PrintStream print, UnitType unit) {

		StringBuilder sb = new StringBuilder();
		
		for (int q = 0; q < unit.getMembers().size(); q++) {
			Person p = unit.getMembers().get(q);
			sb.append(String.format(",s,%d", p.getId()));			
			print.print(String.format("S,%d,%s,%s,%d,%b,%d", p.getId(), p.getName(),
					p.getRank(), p.getScore(), p.isAlive(), p.getJoinTime()));
			for (int w = 0; w < p.getBattles().size(); w++) {
				int[] b = p.getBattles().get(w);
				print.print(String.format(",%d,%d", b[0], b[1]));
			}
			print.print(",p");
			for (int w = 0; w < p.getPromotions().size(); w++) {
				int prom = p.getPromotions().get(w);
				print.print(String.format(",%d", prom));
			}
			print.println();
		}
		for (int q = 0; q < unit.getSubUnits().size(); q++) {
			UnitType u = unit.getSubUnits().get(q);
			sb.append(String.format(",u,%d", u.getId()));
			saveHelper(print, u);
		}
		
		print.print(String.format("U,%d,%s,%s,%d", unit.getId(), unit.getName(),
				unit.getOfficerTitle(), unit.getLeadingSubUnit()));
		print.println(sb.toString());
		
	}
	
	public static void loadStats(File file) throws FileNotFoundException {
		Scanner scan = new Scanner(file);
		scan.useDelimiter(",");
		Person.currentId = Integer.parseInt(scan.next().trim());
		UnitType.currentId = Integer.parseInt(scan.next().trim());

		ArrayList<BattleStats> battles = new ArrayList<>();
		HashMap<Integer, Person> people = new HashMap<>();
		HashMap<Integer, UnitType> units = new HashMap<>();
		while (scan.hasNextLine()) {
			String line = scan.nextLine().trim();
			char c = line.charAt(0);
			if (c == 'B') {
				battles.add(readBattle(line));
			} else if (c == 'S') {
				Person s = readSoldier(line);
				people.put(s.getId(), s);
			} else if (c == 'U') {
				UnitType u = readUnit(line, people, units);
				units.put(u.getId(), u);
			} else if (c == 'T') {
				BattleStatsManager.trackedUnit = readUnit(line, people, units);
			}
		}
		BattleStatsManager.battles = battles;
		scan.close();
	}

	private static UnitType readUnit(String line, HashMap<Integer, Person> people,
			HashMap<Integer, UnitType> units) {
		Scanner scan = new Scanner(line);
		scan.useDelimiter(",");
		//Remove type indication character
		scan.next();
		int id = scan.nextInt();
		String name = scan.next();
		String officer = scan.next();
		int lead = scan.nextInt();
		UnitType ret = new UnitType(id, name, officer, lead);
		while (scan.hasNext()) {
			String indicator = scan.next();
			int key = scan.nextInt();
			if (indicator.equals("s")) {
				Person p = people.get(key);
				ret.addMember(p);
			} else if (indicator.equals("u")) {
				UnitType u = units.get(key);
				ret.addUnit(u);
			}
		}
		
		scan.close();
		return ret;
	}

	private static Person readSoldier(String line) {
		Scanner scan = new Scanner(line);
		scan.useDelimiter(",");
		//Remove type indication character
		scan.next();
		int id = scan.nextInt();
		String name = scan.next();
		if (name != null && name.equals("null")) {
			name = null;
		}
		String rank = scan.next();
		if (rank != null && rank.equals("null")) {
			rank = null;
		}
		int score = scan.nextInt();
		boolean alive = scan.nextBoolean();
		int join = scan.nextInt();
		Person ret = new Person(id, name, rank, score, alive, join);
		while (scan.hasNext()) {
			String next = scan.next();
			//A sloppy way of doing it, but it works
			if (next.equals("p")) {
				break;
			}
			int[] battle = {Integer.parseInt(next), scan.nextInt()};
			ret.addBattle(battle);
		}
		while (scan.hasNext()) {
			ret.getPromotions().add(scan.nextInt());
		}
		scan.close();
		return ret;
	}

	private static BattleStats readBattle(String line) {
		Scanner scan = new Scanner(line);
		scan.useDelimiter(",");
		//Remove type indication character
		scan.next();
		int id = scan.nextInt();
		String name = scan.next();
		int kills = scan.nextInt();
		int deaths = scan.nextInt();
		int allies = scan.nextInt();
		BattleStats ret = new BattleStats(id, name, kills, deaths, allies);
		
		scan.close();
		return ret;
	}
	
	public static ArrayList<String> getNamesFromFile(File file) throws FileNotFoundException {
		ArrayList<String> ret = new ArrayList<>();
		Scanner scan = new Scanner(file);
		
		while (scan.hasNextLine()) {
			ret.add(scan.nextLine());
		}
		
		scan.close();
		return ret;
	}
	
}
