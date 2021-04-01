package edu.ufl.cise.cs1.controllers;
import game.controllers.AttackerController;
import game.models.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public final class StudentAttackerController implements AttackerController
{
	public void init(Game game) { }

	public void shutdown(Game game) { }

	private int getDirectionByPills(Attacker gator, List<Node> pills) {
		// this should never happen as it resets
		if (pills.size() == 0) {
			return Game.Direction.EMPTY;
		}

		Node closestPill = gator.getTargetNode(pills, true);

		return gator.getNextDir(closestPill, true);
	}

	private int getDirectionByPowerPills(Game game, Node curLocation, Attacker gator, List<Node> powerPills, int range) {
		// no more power pills left
		if (powerPills.size() == 0) {
			return Game.Direction.EMPTY;
		}

		Node closestPowerPill = gator.getTargetNode(powerPills, true);
		int distance = curLocation.getPathDistance(closestPowerPill);

		// distance couldn't be computed
		if (distance == -1) {
			return Game.Direction.EMPTY;
		}

		if (distance < range) {
			gator.addPathTo(game, Color.RED, closestPowerPill);
			return gator.getNextDir(closestPowerPill, true);
		}

		return Game.Direction.EMPTY;
	}

	private int getDirectionByDefenders(Game game, Node curLocation, Attacker gator, List<Defender> defenders, List<Node> pills) {
		Node closestDefenderLocation = gator.getTargetNode(getDefenderLocations(defenders), true);
		int distance = curLocation.getPathDistance(closestDefenderLocation);

		// distance couldn't be computed
		if (distance == -1) {
			return Game.Direction.EMPTY;
		}

		Defender d = getDefenderByLocation(defenders, closestDefenderLocation);

		// eat the close vulnerable defenders
		if (distance < 15 && d.isVulnerable() && d.getVulnerableTime() > 5) {
			gator.addPathTo(game, Color.ORANGE, closestDefenderLocation);
			return gator.getNextDir(closestDefenderLocation, true);
		}

		// close defender that is dangerous
		if (distance < 10) {
			return avoidDefender(game, curLocation, gator, closestDefenderLocation, pills);
		}

		return Game.Direction.EMPTY;
	}

	// TODO - need to handle multiple defenders
	private int avoidDefender(Game game, Node curLocation, Attacker gator, Node closestDefenderLocation, List<Node> pills) {
		int defaultAvoidance = gator.getNextDir(closestDefenderLocation, false);

		// get the attacker direction and try to go to a pill in a different direction
		int attackerDirection = gator.getNextDir(closestDefenderLocation, true);
		int toNextPill = getDirectionByPills(gator, pills);

		if (toNextPill != attackerDirection) {
			return toNextPill;
		}

		// default avoidance
		return defaultAvoidance;
	}

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

		// it should always find the correct defender but if a bad location is passed as argument
		// print a warning and return the first defender
		System.out.println("No defender found!");
		return defenders.get(0);
	}

	public int update(Game game, long timeDue) {
		Attacker gator = game.getAttacker();

		// get current map information
		Node curLocation = gator.getLocation();
		List<Defender> defenders = game.getDefenders();
		List<Node> powerPills = game.getPowerPillList();
		List<Node> pills = game.getPillList();

		// very close to power pill -> so get it
		int immediatePowerPill = getDirectionByPowerPills(game, curLocation, gator, powerPills, 10);
		if (immediatePowerPill != -1) {
			return immediatePowerPill;
		}

		// immediately under pressure or able to eat a somewhat close vulnerable defender
		int moveByDefender = getDirectionByDefenders(game, curLocation, gator, defenders, pills);
		if (moveByDefender != -1) {
			return moveByDefender;
		}

		// there is an medium range power pill available
		int intermediatePowerPill = getDirectionByPowerPills(game, curLocation, gator, powerPills, 60);
		if (intermediatePowerPill != -1) {
			return intermediatePowerPill;
		}

		// just get the next normal pill
		return getDirectionByPills(gator, pills);
	}
}