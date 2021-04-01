package edu.ufl.cise.cs1.controllers;
import game.controllers.AttackerController;
import game.models.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/* This is the only place where code where be added */
public final class StudentAttackerController implements AttackerController
{
	public void init(Game game) { }

	public void shutdown(Game game) { }

	private int makeDecision(Node curLocation, Attacker gator, List<Node> powerPills, List<Defender> defenders, List<Node> pills) {
//		int directionToPowerPill = getDirectionByPowerPills(curLocation, gator, powerPills);
//		if (directionToPowerPill != -1) {
//			return directionToPowerPill;
//		}
//
//		int moveByDefender = getDirectionByDefenders(curLocation, gator, defenders);
//		if (moveByDefender != -1) {
//			return moveByDefender;
//		}

		return getDirectionByPills(gator, pills);
	}

	// most of the decisioning logic will live here
	private int getDirection(
			Game game,
			Attacker gator,
			List<Node> powerPills,
			List<Defender> defenders,
			List<Node> pills,
			Node curLocation
	) {
		List<Integer> possibleDirs = gator.getPossibleDirs(false);
		int currentDirection = gator.getDirection();

		if (curLocation.isJunction()) {
			return makeDecision(curLocation, gator, powerPills, defenders, pills);
		}

		// can't keep going current direction
		if (!possibleDirs.contains(currentDirection)) {
			return makeDecision(curLocation, gator, powerPills, defenders, pills);
		}

		return Game.Direction.EMPTY;
	}

	private int getDirectionByPills(Attacker gator, List<Node> pills) {
		// this should never happen as it resets
		if (pills.size() == 0) {
			return Game.Direction.EMPTY;
		}

		Node closestPowerPill = gator.getTargetNode(pills, true);

		return gator.getNextDir(closestPowerPill, true);
	}

	private int getDirectionByPowerPills(Node curLocation, Attacker gator, List<Node> powerPills) {
		int action = Game.Direction.EMPTY;
		if (powerPills.size() == 0) {
			return action;
		}

		Node closestPowerPill = gator.getTargetNode(powerPills, true);
		int distance = curLocation.getPathDistance(closestPowerPill);

		if (distance != -1 && distance < 25) {
			return gator.getNextDir(closestPowerPill, true);
		}

		return action;
	}

	private int getDirectionByDefenders(Node curLocation, Attacker gator, List<Defender> defenders) {
		Node closestDefenderLocation = gator.getTargetNode(getDefenderLocations(defenders), true);
		int distance = curLocation.getPathDistance(closestDefenderLocation);

		// -1 means the distance couldn't be computed
		if (distance == -1) {
			return Game.Direction.EMPTY;
		}

		Defender d = getDefenderByLocation(defenders, closestDefenderLocation);

		// eat the vunerable defenders
		if (distance < 20 && d.isVulnerable() && d.getVulnerableTime() > 5) {
			return gator.getNextDir(closestDefenderLocation, true);
		}

		// close defender that is dangerous
		if (distance < 5) {
			return gator.getNextDir(closestDefenderLocation, false);
		}

		return Game.Direction.EMPTY;
	}

	// java 8 stream https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html
	private List<Node> getDefenderLocations(List<Defender> defenders) {
		return defenders.stream().map(Defender::getLocation).collect(Collectors.toList());
	}

	private List<Defender> getVunerableDefenders(List<Defender> defenders) {
		return defenders.stream().filter(Defender::isVulnerable).collect(Collectors.toList());
	}

	private Defender getDefenderByLocation(List<Defender> defenders, Node n) {
		int nodeX = n.getX();
		int nodeY = n.getY();

		for (Defender d: defenders) {
			Node location = d.getLocation();
			if (location.getX() == nodeX && location.getY() == nodeY) {
				return d;
			}
		}

		// it should always find the correct defender but if a bad location is returned
		// print a warning and return the first defender
		System.out.println("No defender found!");
		return defenders.get(0);
	}

	public int update(Game game, long timeDue) {
		// default action is empty and attacker to be controlled
		int action = Game.Direction.EMPTY;
		Attacker gator = game.getAttacker();
		// dont allow reversing
		List<Integer> possibleDirs = gator.getPossibleDirs(true);

		// gator can't move anywhere
		if (possibleDirs.size() == 0) {
			return action;
		}

		// get current map information
		Node curLocation = gator.getLocation();
		List<Defender> defenders = game.getDefenders();
		List<Node> powerPills = game.getPowerPillList();
		List<Node> pills = game.getPillList();

		action = getDirection(game, gator, powerPills, defenders, pills, curLocation);

		return action;
	}
}