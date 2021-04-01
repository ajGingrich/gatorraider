package edu.ufl.cise.cs1.controllers;
import game.controllers.AttackerController;
import game.models.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;


//		gator.addPathTo(game, Color.BLUE, closestDefenderLocation);

/* This is the only place where code where be added */
public final class StudentAttackerController implements AttackerController
{
	public void init(Game game) { }

	public void shutdown(Game game) { }

	// most of the decisioning logic will live here
	private int getDirection(
			Game game,
			Attacker gator,
			List<Node> powerPills,
			List<Defender> defenders,
			List<Node> pills,
			Node curLocation
	) {
		int action = Game.Direction.EMPTY;
		// TODO: it gets confused with multiple defenders
		// TODO: dont switch direction too much - it toggles

		if (powerPills.size() != 0) {
			Node closestPowerPill = gator.getTargetNode(powerPills, true);
			int closePowerPillDistance = curLocation.getPathDistance(closestPowerPill);

			// get the closest power pills
			if (closePowerPillDistance != -1 && closePowerPillDistance < 20) {
				return gator.getNextDir(closestPowerPill, true);
			}
		}

		Node closestDefenderLocation = gator.getTargetNode(getDefenderLocations(defenders), true);
		int distance = curLocation.getPathDistance(closestDefenderLocation);

		// if there is a close defender -> this will take priority.
		// Either avoid it or get close to it depending on its state
		// -1 means the distance couldn't be computed
		if (distance != -1 && distance < 75) {
			Defender d = getDefenderByLocation(defenders, closestDefenderLocation);
			boolean shouldApproach = d.isVulnerable() && d.getVulnerableTime() > 15;
			return gator.getNextDir(closestDefenderLocation, shouldApproach);
		}

		if (powerPills.size() != 0) {
			Node closestPowerPill = gator.getTargetNode(powerPills, true);
			return gator.getNextDir(closestPowerPill, true);
		}

		if (pills.size() != 0) {
			Node pill = gator.getTargetNode(pills, true);
			return gator.getNextDir(pill, true);
		}

		return Game.Direction.EMPTY;
	}

	private int getDirectionByDefenders() { return -1; }

	// java 8 stream https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html
	private List<Node> getDefenderLocations(List<Defender> defenders) {
		return defenders.stream().map(Defender::getLocation).collect(Collectors.toList());
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