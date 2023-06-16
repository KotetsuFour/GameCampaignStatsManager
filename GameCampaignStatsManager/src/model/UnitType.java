package model;

import java.util.ArrayList;

public class UnitType {

	public static int currentId = 0;
	private int id;
	private String name;
	private ArrayList<UnitType> subUnits;
	private ArrayList<Person> members;
	private String officerTitle;
	private int leadingSubUnit; //-1 for individual soldier
	private boolean participating;
	
	public UnitType() {
		this.id = currentId;
		currentId++;
		this.subUnits = new ArrayList<>();
		this.members = new ArrayList<>();
	}
	
	/**
	 * Constructor for loading from file, not for initialization
	 * @param id
	 * @param name
	 * @param officer
	 * @param lead
	 */
	public UnitType(int id, String name, String officer, int lead) {
		this.id = id;
		this.name = name;
		this.officerTitle = officer;
		this.leadingSubUnit = lead;
		subUnits = new ArrayList<>();
		members = new ArrayList<>();
	}
	
	public void addUnit(UnitType u) {
		subUnits.add(u);
	}
	
	public void addMember() {
		members.add(new Person());
	}
	public void addMember(int battlesPassed) {
		members.add(new Person(battlesPassed));
	}
	/**
	 * For loading from file
	 * @param p
	 */
	public void addMember(Person p) {
		members.add(p);
	}
	
	public UnitType clone() {
		//TODO something causes officers above the lowest level to have no title
		UnitType ret = new UnitType();
		ret.setName(name);
		for (int q = 0; q < members.size(); q++) {
			ret.addMember();
		}
		for (int q = 0; q < subUnits.size(); q++) {
			ret.addUnit(subUnits.get(q).clone());
		}
		ret.setOfficer(officerTitle, leadingSubUnit);
//		if (leadingSubUnit == -1) {
//			ret.getMembers().get(0).setRank(officerTitle);
//		}
		
		return ret;
	}
	
	public void print() {
		for (int q = 0; q < members.size(); q++) {
			members.get(q).print();
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).print();
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public boolean isEmpty() {
		return subUnits.isEmpty() && members.isEmpty();
	}
	public ArrayList<UnitType> getSubUnits() {
		return subUnits;
	}
	public ArrayList<Person> getMembers() {
		return members;
	}
	public void setOfficer(String title, int index) {
		this.officerTitle = title;
		this.leadingSubUnit = index;
		if (index == -1) {
			members.get(0).setRank(title);
		} else {
			subUnits.get(index).setOfficer(title);
		}
	}
	private void setOfficer(String title) {
		this.officerTitle = title;
		if (leadingSubUnit == -1) {
			members.get(0).setRank(title);
		} else {
			subUnits.get(leadingSubUnit).setOfficer(title);
		}
	}
	public String getOfficerTitle() {
		return officerTitle;
	}
	public int getLeadingSubUnit() {
		return leadingSubUnit;
	}
	
	public void resetIds() {
		for (int q = 0; q < members.size(); q++) {
			members.get(q).resetId();
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).resetIds();
		}
	}
	
	public void setParticipating(boolean participating) {
		this.participating = participating;
		for (int q = 0; q < members.size(); q++) {
			members.get(q).setParticipating(participating);
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).setParticipating(participating);
		}
	}
	public boolean isParticipating() {
		return participating;
	}
	
	public ArrayList<Person> getAllPeople() {
		ArrayList<Person> ret = new ArrayList<>();
		for (int q = 0; q < members.size(); q++) {
			ret.add(members.get(q));
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).getAllPeopleHelper(ret);
		}
		return ret;
	}
	private void getAllPeopleHelper(ArrayList<Person> ret) {
		for (int q = 0; q < members.size(); q++) {
			ret.add(members.get(q));
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).getAllPeopleHelper(ret);
		}
	}
	
	public void setAllUnsafe() {
		for (int q = 0; q < members.size(); q++) {
			members.get(q).setSafe(false);
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).setAllUnsafe();
		}
	}
	
	public String getNumbers() {
		ArrayList<Integer> counts = new ArrayList<>();
		ArrayList<String> ranks = new ArrayList<>();
		ArrayList<String> named = new ArrayList<>();
		ranks.add("Common Soldier");
		counts.add(0);
		for (int q = 0; q < members.size(); q++) {
			Person p = members.get(q);
			if (p.isAlive()) {
				if (p.getRank() == null) {
					counts.set(0, counts.get(0) + 1);
				} else {
					boolean found = false;
					for (int w = 0; w < ranks.size(); w++) {
						if (ranks.get(w).equals(p.getRank())) {
							counts.set(w, counts.get(w) + 1);
							found = true;
						}
					}
					if (!found) {
						ranks.add(p.getRank());
						counts.add(1);
					}
				}
				if (p.getName() != null && p.getName().charAt(0) != '#') {
					named.add(p.toString());
				}
			}
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).getNumbersHelper(counts, ranks, named);
		}
		
		int total = 0;
		for (int q = 0; q < counts.size(); q++) {
			total += counts.get(q);
		}
		
		StringBuilder sb = new StringBuilder("<html>" + total + " living soldiers, including:<br/>");
		for (int q = 0; q < ranks.size(); q++) {
			sb.append("<html>" + counts.get(q) + " " + ranks.get(q) + "<br/>");
		}
		sb.append("<html><br/><html>This includes the following:<br/>");
		for (int q = 0; q < named.size(); q++) {
			sb.append("<html>" + named.get(q) + "<br/>");
		}
		
		return sb.toString();
	}
	private void getNumbersHelper(ArrayList<Integer> counts,
			ArrayList<String> ranks, ArrayList<String> named) {
		for (int q = 0; q < members.size(); q++) {
			Person p = members.get(q);
			if (p.isAlive()) {
				if (p.getRank() == null) {
					counts.set(0, counts.get(0) + 1);
				} else {
					boolean found = false;
					for (int w = 0; w < ranks.size(); w++) {
						if (ranks.get(w).equals(p.getRank())) {
							counts.set(w, counts.get(w) + 1);
							found = true;
						}
					}
					if (!found) {
						ranks.add(p.getRank());
						counts.add(1);
					}
				}
				if (p.getName() != null) {
					named.add(p.toString());
				}
			}
		}
		for (int q = 0; q < subUnits.size(); q++) {
			subUnits.get(q).getNumbersHelper(counts, ranks, named);
		}
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
//	public static void main(String[] args) {
//		UnitType squad = new UnitType("Squad", 9, "Sergeant");
//		UnitType platoon = new UnitType("Platoon", 4, squad, "Lieutenant");
//		UnitType company = new UnitType("Company", 4, platoon, "Captain");
//		UnitType battalion = new UnitType("Battalion", 4, company, "Battalion Commander");
//		UnitType regiment = new UnitType("Regiment", 4, battalion, "Regiment Commander");
//		UnitType legion = new UnitType("Legion", 4, regiment, "Legion Commander");
//		legion.print();
//	}
}
