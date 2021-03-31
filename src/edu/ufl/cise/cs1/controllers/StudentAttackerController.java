package edu.ufl.cise.cs1.controllers;
import game.controllers.AttackerController;
import game.models.*;
import java.awt.*;
import java.util.List;

/* This is the only place where code where be added */
public final class StudentAttackerController implements AttackerController
{
	public void init(Game game) { }

	public void shutdown(Game game) { }

	public int update(Game game,long timeDue)
	{
		// default action is empty
		int action = Game.Direction.EMPTY;
		// the actor to be controlled
		Attacker gator = game.getAttacker();

		// An example (which should not be in your final submission) of some syntax that randomly chooses a direction for the attacker to move
		List<Integer> possibleDirs = gator.getPossibleDirs(true);

		// the gator can't move anywhere
		if (possibleDirs.size() == 0) {
			return action;
		}

		// TODO: get a list of the all the power pills/defenders/pills
		// figure out which is the closest with the
		List<Node> powerPills = game.getPowerPillList();
		List<Defender> defenders = game.getDefenders();
		List<Node> regularPills = game.getPillList();

		// get the closest power pills
		if (powerPills.size() != 0) {
			Node closestPowerPill = gator.getTargetNode(powerPills, true);
			// use this to determine the direction for the defenders/pills
			action = gator.getNextDir(closestPowerPill, true);
			gator.addPathTo(game, Color.BLUE, closestPowerPill);
		}

		game.getDefenders();


//		action = possibleDirs.get(Game.rng.nextInt(possibleDirs.size()));


		// actor
		// getLocation - returns current location of the actor

		// game
		// getDefenders

		// defender interface
		// isVulnerable

		// with two nodes -> get path distance

		// An example (which should not be in your final submission) of some syntax to use the visual debugging method, addPathTo, to the top left power pill.


		return action;
	}
}