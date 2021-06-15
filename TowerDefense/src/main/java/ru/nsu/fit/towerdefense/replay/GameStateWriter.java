package ru.nsu.fit.towerdefense.replay;

import ru.nsu.fit.towerdefense.simulator.world.gameobject.Base;
import ru.nsu.fit.towerdefense.simulator.world.gameobject.Enemy;
import ru.nsu.fit.towerdefense.simulator.world.gameobject.Projectile;
import ru.nsu.fit.towerdefense.simulator.world.gameobject.Tower;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class GameStateWriter {


	private static GameStateWriter instance = null;

	private XMLOutputFactory factory = null;

	private XMLStreamWriter writer = null;

	private String replayDir = "./Replays/";

	private int currentTick = 0;
	private int fullCopy = 360;
	String tab = "\t\t\t\t\t\t\t";
	private GameStateWriter()
	{
		factory = XMLOutputFactory.newFactory();
	}

	public static GameStateWriter getInstance()
	{
		if (instance == null) instance = new GameStateWriter();
		return instance;
	}

	public void startNewReplay(int tickRate, String levelName)
	{
		try
		{
			fullCopy = tickRate;
			String dir = replayDir + levelName + "/";
			File parent = new File(dir);
			if (!parent.exists() && !parent.mkdirs())
			{
				System.out.println("Unable to create file");
				return;
			}
			int id = parent.list().length;
			File file = new File(dir + "/Replay_" + id + ".xml");
			file.createNewFile();
			writer = factory.createXMLStreamWriter(new FileWriter(file));
			writer.writeStartDocument();
			writer.writeCharacters(System.getProperty("line.separator"));
			writer.writeStartElement("Replay");
			writer.writeAttribute("tickRate", Integer.toString(tickRate));
			writer.writeCharacters(System.getProperty("line.separator"));
			currentTick = 0;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

  	}

	public void newFrame()
	{
		try
		{
			writer.writeCharacters(tab.substring(0, 1));
			writer.writeStartElement("Frame");
			writer.writeAttribute("frameId", Integer.toString(currentTick));
			writer.writeAttribute("isFullCopy", Boolean.toString((currentTick % fullCopy) == 0));
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void fullCopy(List<Enemy> enemies, List<Tower> towers, List<Projectile> projectiles,
			Base base, int money, int waveNumber, int currentEnemyNumber, int countdown, int science, int killedEnemies)
	{
		if (currentTick % fullCopy != 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("Base");
			writer.writeAttribute("Health", Integer.toString(base.getHealth()));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
			for (var x : enemies)
			{
				if (x.isDead()) continue;
				writer.writeCharacters(tab.substring(0, 2));
				writer.writeStartElement("Enemy");
				writer.writeAttribute("EnemyId", Integer.toString(x.getId()));
				writer.writeAttribute("Health", Integer.toString(x.getHealth()));
				writer.writeAttribute("PosX", Double.toString(x.getPosition().getX()));
				writer.writeAttribute("PosY", Double.toString(x.getPosition().getY()));
				writer.writeAttribute("Type", x.getType().getTypeName());
				writer.writeAttribute("Wave", Integer.toString(x.getWaveNumber()));
				writer.writeAttribute("Reward", Integer.toString(x.getMoneyReward()));
				writer.writeCharacters(System.getProperty("line.separator"));
				writer.writeCharacters(tab.substring(0, 3));
				writer.writeStartElement("Trajectory");
				writer.writeCharacters(System.getProperty("line.separator"));
				for (var y : x.getTrajectory())
				{
					writer.writeCharacters(tab.substring(0, 4));
					writer.writeStartElement("Point");
					writer.writeAttribute("PosX", Double.toString(y.getX()));
					writer.writeAttribute("PosY", Double.toString(y.getY()));
					writer.writeEndElement();
					writer.writeCharacters(System.getProperty("line.separator"));
				}
				writer.writeCharacters(tab.substring(0, 3));
				writer.writeEndElement();
				writer.writeCharacters(System.getProperty("line.separator"));
				writer.writeCharacters(tab.substring(0, 3));
				writer.writeStartElement("Effects");
				writer.writeCharacters(System.getProperty("line.separator"));
				for (var y : x.getEffects())
				{
					writer.writeCharacters(tab.substring(0, 4));
					writer.writeStartElement("Effect");
					writer.writeAttribute("Name", y.getType().getName());
					writer.writeAttribute("Duration", Integer.toString(y.getRemainingTicks()));
					writer.writeEndElement();
					writer.writeCharacters(System.getProperty("line.separator"));
				}
				writer.writeCharacters(tab.substring(0, 3));
				writer.writeEndElement();
				writer.writeCharacters(System.getProperty("line.separator"));
				writer.writeCharacters(tab.substring(0, 2));
				writer.writeEndElement();
				writer.writeCharacters(System.getProperty("line.separator"));
			}
			for (var x : towers)
			{
				writer.writeCharacters(tab.substring(0, 2));
				writer.writeStartElement("Tower");
				writer.writeAttribute("TowerId", Integer.toString(x.getId()));
				writer.writeAttribute("Type", x.getType().getTypeName());
				writer.writeAttribute("PosX", Double.toString(x.getPosition().getX()));
				writer.writeAttribute("PosY", Double.toString(x.getPosition().getY()));
				writer.writeAttribute("Mode", x.getMode().name());
				writer.writeAttribute("Rotation", Double.toString(x.getRotation()));
				writer.writeAttribute("Cooldown", Integer.toString(x.getCooldown()));
				writer.writeAttribute("SellPrice", Integer.toString(x.getSellPrice()));
				String target = "";
				if (x.getTarget() != null && !x.getTarget().isDead())
					target = Integer.toString(x.getTarget().getId());
				else
					target = "None";
				writer.writeAttribute("TargetId", target);
				writer.writeEndElement();
				writer.writeCharacters(System.getProperty("line.separator"));
			}

			for (var x : projectiles)
			{
				if (x.getRemainingRange() <= 0.0) continue;
				writer.writeCharacters(tab.substring(0, 2));
				writer.writeStartElement("Projectile");
				writer.writeAttribute("ProjectileId", Integer.toString(x.getId()));
				String target = "";
				if (x.getTarget() != null && !x.getTarget().isDead())
					target = Integer.toString(x.getTarget().getId());
				else
					target = "None";
				writer.writeAttribute("TargetId", target);
				writer.writeAttribute("PosX", Double.toString(x.getPosition().getX()));
				writer.writeAttribute("PosY", Double.toString(x.getPosition().getY()));
				writer.writeAttribute("Type", x.getType().getTypeName());
				writer.writeAttribute("RemainingRange", Double.toString(x.getRemainingRange()));
				writer.writeAttribute("VelX", Double.toString(x.getVelocity().getX()));
				writer.writeAttribute("VelY", Double.toString(x.getVelocity().getY()));
				writer.writeAttribute("FireType", x.getFireType().name());
				writer.writeAttribute("Scale", Double.toString(x.getScale()));
				writer.writeAttribute("ParentX", Double.toString(x.getParentPosition().getX()));
				writer.writeAttribute("ParentY", Double.toString(x.getParentPosition().getY()));
				writer.writeEndElement();
				writer.writeCharacters(System.getProperty("line.separator"));
			}

			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("Money");
			writer.writeAttribute("amount", Integer.toString(money));
			writer.writeEndElement();
			writer.writeStartElement("Science");
			writer.writeAttribute("amount", Integer.toString(science));
			writer.writeEndElement();
			writer.writeStartElement("KilledEnemies");
			writer.writeAttribute("amount", Integer.toString(killedEnemies));
			writer.writeEndElement();
			writer.writeStartElement("CurrentWave");
			writer.writeAttribute("waveNumber", Integer.toString(waveNumber));
			writer.writeAttribute("countdown", Integer.toString(countdown));
			writer.writeAttribute("currentEnemyNumber", Integer.toString(currentEnemyNumber));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));

		}
		catch (Exception e)
		{

		}
	}

	public void buildTower(int position, String typeName, String id)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("BuildTower");
			writer.writeAttribute("Position", Integer.toString(position));
			writer.writeAttribute("Type", typeName);
			writer.writeAttribute("ID", id);
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void upgradeTower(int position, String newType)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("UpgradeTower");
			writer.writeAttribute("Position", Integer.toString(position));
			writer.writeAttribute("NewType", newType);
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void callWave()
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("CallWave");
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void removeEnemy(Enemy enemy)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("RemoveEnemy");
			writer.writeAttribute("EnemyId", Integer.toString(enemy.getId()));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void removeProjectile(Projectile projectile)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("RemoveProjectile");
			writer.writeAttribute("ProjectileId", Integer.toString(projectile.getId()));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void switchMode(Tower tower, Tower.Mode newMode)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("SwitchMode");
			writer.writeAttribute("TowerId", Integer.toString(tower.getId()));
			writer.writeAttribute("Mode", newMode.name());
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void dealDamageEnemy(Enemy enemy, int damage)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("DealDamage");
			writer.writeAttribute("EnemyId", Integer.toString(enemy.getId()));
			writer.writeAttribute("Damage", Integer.toString(damage));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void dealDamageBase(int damage)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("DealDamageBase");
			writer.writeAttribute("Damage", Integer.toString(damage));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void sellTower(int position)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("SellTower");
			writer.writeAttribute("Position", Integer.toString(position));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void applyEffect(Enemy enemy, String effectName)
	{
		if (currentTick % fullCopy == 0)
			return;
		try
		{
			writer.writeCharacters(tab.substring(0, 2));
			writer.writeStartElement("Apply");
			writer.writeAttribute("Id", Integer.toString(enemy.getId()));
			writer.writeAttribute("Effect", effectName);
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{

		}
	}

	public void endFrame()
	{
		try
		{
			writer.writeCharacters(tab.substring(0, 1));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
			currentTick++;
		}
		catch (Exception e)
		{
		}
	}


	public void close()
	{
		try
		{
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
			writer.writeEndDocument();
			writer.close();
		}
		catch (Exception e)
		{

		}
	}

	public void saveGame(String levelName, List<Enemy> enemies, List<Tower> towers, List<Projectile> projectiles,
			Base base, int money, int waveNumber, int currentEnemyNumber, int countdown, int science, int killedEnemies)
	{
		try
		{
			String name = "./Saves/" + levelName + "/save.xml";
			File file = new File(name);
			File parent = file.getParentFile();
			if (!parent.exists() && !parent.mkdirs())
			{
				System.out.println("Unable to create file");
			}
			file.createNewFile();
			writer = factory.createXMLStreamWriter(new FileWriter(file));
			writer.writeStartDocument();
			writer.writeCharacters(System.getProperty("line.separator"));
			writer.writeStartElement("Save");
			writer.writeCharacters(System.getProperty("line.separator"));
			writer.writeCharacters(tab.substring(0, 1));
			writer.writeStartElement("WorldState");
			writer.writeCharacters(System.getProperty("line.separator"));
			fullCopy(enemies, towers, projectiles, base, money, waveNumber, currentEnemyNumber, countdown, science, killedEnemies);
			writer.writeCharacters(tab.substring(0, 1));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
			writer.writeEndElement();
			writer.writeCharacters(System.getProperty("line.separator"));
			writer.writeEndDocument();
			writer.close();

		}
		catch (Exception e)
		{

		}
	}


}
