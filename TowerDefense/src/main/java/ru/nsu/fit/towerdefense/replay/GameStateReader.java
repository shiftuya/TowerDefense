package ru.nsu.fit.towerdefense.replay;

import javafx.util.Pair;
import ru.nsu.fit.towerdefense.metadata.gameobjecttypes.TowerType;
import ru.nsu.fit.towerdefense.replay.objectInfo.EnemyInfo;
import ru.nsu.fit.towerdefense.replay.objectInfo.ProjectileInfo;
import ru.nsu.fit.towerdefense.replay.objectInfo.TowerInfo;
import ru.nsu.fit.towerdefense.simulator.world.gameobject.Renderable;
import ru.nsu.fit.towerdefense.util.Vector2;
import ru.nsu.fit.towerdefense.simulator.world.gameobject.Tower;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameStateReader {

	private static GameStateReader instance = null;

	private XMLInputFactory factory = null;

	private XMLStreamReader reader = null;

	private String replayDir = "./Replays/";
	private String saveDir = "./Saves/";

	private GameStateReader()
	{
		factory = XMLInputFactory.newFactory();
	}

	public static GameStateReader getInstance()
	{
		if (instance == null) instance = new GameStateReader();
		return instance;
	}

	public String[] getReplays(String level)
	{
		String dir = replayDir + level + "/";

		File parent = new File(dir);
		if (!parent.exists() && !parent.mkdirs())
		{
			return new String[0];
		}
		return parent.list();
	}

	public String[] getSaves()
	{
		File file = new File(saveDir);
		if (!file.exists() && !file.mkdirs())
		{
			return new String[0];
		}
		return file.list();
	}

	public WorldState loadSave(String level)
	{
		try
		{
			String dir = saveDir + level + "/save.xml";
			File file = new File(dir);
			reader = factory.createXMLStreamReader(new FileInputStream(file));

			reader.next();
			reader.next();
			reader.next();
			reader.next();
			return parseState(-1, "WorldState");

		}
		catch (Exception e)
		{
			return null;
		}
	}

	public Replay readReplay(String level, String name)
	{
		try
		{
			String dir = replayDir + level + "/" + name;
			File file = new File(dir);
			ArrayList<WorldState> states = new ArrayList<>();
			ArrayList<EventRecord> records = new ArrayList<>();

			reader = factory.createXMLStreamReader(new FileInputStream(file));
			int tickRate = 0;

			reader.next();
			tickRate = Integer.parseInt(reader.getAttributeValue(0));
			reader.next();
			while (reader.hasNext())
			{
				if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
				{
					int id = Integer.parseInt(reader.getAttributeValue(0));
					String type = reader.getAttributeValue(1);
					reader.next();
					if (type.equals("true"))
					{
						states.add(parseState(id, "Frame"));
					}
					else
					{
						records.add(parseRecord(id));
					}
				}
				else
				{
					reader.next();
				}
			}
			Replay replay = new Replay(tickRate, records, states);
			return replay;
		}
		catch (Exception e)
		{
			return null;
		}
	}


	private WorldState parseState(int fid, String tag)
	{
		try
		{
			ArrayList<EnemyInfo> enemies = new ArrayList<>();
			ArrayList<TowerInfo> towers = new ArrayList<>();
			ArrayList<ProjectileInfo> projectiles = new ArrayList<>();
			int money = 0;
			int health = 0;
			int waveNumber = 0;
			int currentEnemyNumber = 0;
			int countdown = 0;
			while (true)
			{
				int event = reader.getEventType();
				if (event == XMLStreamConstants.START_ELEMENT)
				{
					switch (reader.getLocalName())
					{
						case "Base" -> health = Integer.parseInt(reader.getAttributeValue(0));
						case "Enemy" -> enemies.add(parseEnemy());
						case "Tower" -> towers.add(parseTower());
						case "Projectile" -> projectiles.add(parseProjectile());
						case "Money" -> money = Integer.parseInt(reader.getAttributeValue(0));
						case "CurrentWave" -> {
							waveNumber = Integer.parseInt(reader.getAttributeValue(0));
							countdown = Integer.parseInt(reader.getAttributeValue(1));
							currentEnemyNumber = Integer.parseInt(reader.getAttributeValue(2));
						}
					}
				}
				else if (event == XMLStreamConstants.END_ELEMENT)
				{
					if (reader.getLocalName().equals(tag)) break;
				}
				reader.next();
			}
			return new WorldState(enemies, towers, projectiles, money, fid, health, waveNumber, currentEnemyNumber, countdown);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private EnemyInfo parseEnemy()
	{
		try
		{
			String type = "";
			Vector2<Double> position = new Vector2<>(0.0, 0.0);
			int wave = 0;
			String id = "";
			int health = 0;
			ArrayList<Vector2<Double>> trajectory = new ArrayList<>();
			EnemyInfo info = new EnemyInfo();
			ArrayList<Pair<String, Integer>> effects = new ArrayList<>();
			while (true)
			{
				int event = reader.getEventType();
				if (event == XMLStreamConstants.START_ELEMENT)
				{
					if (reader.getLocalName().equals("Enemy"))
					{
						id = reader.getAttributeValue(0);
						health = Integer.parseInt(reader.getAttributeValue(1));
						info.setHealth(health);
						position.setX(Double.parseDouble(reader.getAttributeValue(2)));
						position.setY(Double.parseDouble(reader.getAttributeValue(3)));

						type = reader.getAttributeValue(4);
						wave = Integer.parseInt(reader.getAttributeValue(5));
					}
					else if (reader.getLocalName().equals("Point")) // trajectory
					{
						trajectory.add(new Vector2<>(Double.parseDouble(reader.getAttributeValue(0)), Double.parseDouble(reader.getAttributeValue(1))));
					}
					else if (reader.getLocalName().equals("Effect"))
					{
						effects.add(new Pair<>(reader.getAttributeValue(0), Integer.parseInt(reader.getAttributeValue(1))));
					}
				}
				else if (event == XMLStreamConstants.END_ELEMENT)
				{
					if (reader.getLocalName().equals("Enemy"))
						break;
				}
				reader.next();
			}
			info.setId(id);
			info.setHealth(health);
			info.setPosition(position);
			info.setType(type);
			info.setWave(wave);
			info.setTrajectory(trajectory);
			info.setEffects(effects);
			return info;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private TowerInfo parseTower()
	{
		try
		{
			TowerInfo info = new TowerInfo();
			info.setId(reader.getAttributeValue(0));
			info.setType(reader.getAttributeValue(1));
			info.setPosition(new Vector2<>(Double.parseDouble(reader.getAttributeValue(2)), Double.parseDouble(reader.getAttributeValue(3))));
			info.setMode(Tower.Mode.valueOf(reader.getAttributeValue(4)));
			info.setRotation(Double.parseDouble(reader.getAttributeValue(5)));
			info.setCooldown(Integer.parseInt(reader.getAttributeValue(6)));
			info.setSellPrice(Integer.parseInt(reader.getAttributeValue(7)));
			info.setTarget(reader.getAttributeValue(8));
			reader.next();
			return info;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private ProjectileInfo parseProjectile()
	{
		try
		{
			ProjectileInfo info = new ProjectileInfo();

			info.setId(reader.getAttributeValue(0));
			info.setTarget(reader.getAttributeValue(1));
			info.setPosition(new Vector2<>(Double.parseDouble(reader.getAttributeValue(2)), Double.parseDouble(reader.getAttributeValue(3))));
			info.setType(reader.getAttributeValue(4));
			info.setRange(Double.parseDouble(reader.getAttributeValue(5)));
			info.setVelocity(new Vector2<>(Double.parseDouble(reader.getAttributeValue(6)), Double.parseDouble(reader.getAttributeValue(7))));
			info.setFireType(TowerType.FireType.valueOf(reader.getAttributeValue(8)));
			info.setScale(Double.parseDouble(reader.getAttributeValue(9)));
			return info;
		}
		catch (Exception e)
		{
			return null;
		}
	}



	private EventRecord parseRecord(int fid)
	{
		try
		{
			List<Pair<Integer, Pair<String, String>>> buildTower = new LinkedList<>();
			List<Pair<Integer, String>> upgradeTower = new LinkedList<>();
			boolean callWave = false;
			List<String> removeEnemy = new LinkedList<>();
			List<String> removeProjectile = new LinkedList<>();
			List<Pair<Integer, String>> enemyDamage = new LinkedList<>();
			List<Integer> damageToBase = new LinkedList<>();
			List<Pair<String, Tower.Mode>> tuneTower = new LinkedList<>();
			List<Integer> sellTower = new LinkedList<>();
			List<Pair<String, String>> applyEffect = new LinkedList<>();
			while (true)
			{
				int eventType = reader.getEventType();
				if (eventType == XMLStreamConstants.START_ELEMENT)
				{
					switch (reader.getLocalName())
					{
						case "BuildTower" -> {
							int position = Integer.parseInt(reader.getAttributeValue(0));
							String type = reader.getAttributeValue(1);
							String id = reader.getAttributeValue(2);
							buildTower.add(new Pair<>(position, new Pair<>(type, id)));
							break;
						}
						case "UpgradeTower" -> {
							int position = Integer.parseInt(reader.getAttributeValue(0));
							String type = reader.getAttributeValue(1);
							upgradeTower.add(new Pair<>(position, type));
							break;
						}
						case "CallWave" -> callWave = true;
						case "RemoveEnemy" -> {
							String id = reader.getAttributeValue(0);
							removeEnemy.add(id);
							break;
						}
						case "RemoveProjectile" -> {
							String id = reader.getAttributeValue(0);
							removeProjectile.add(id);
							break;
						}
						case "DealDamage" -> {
							String id = reader.getAttributeValue(0);
							int amount = Integer.parseInt(reader.getAttributeValue(1));
							enemyDamage.add(new Pair<>(amount, id));
							break;
						}
						case "DealDamageBase" -> {
							int amount = Integer.parseInt(reader.getAttributeValue(0));
							damageToBase.add(amount);
							break;
						}
						case "SellTower" -> sellTower.add(Integer.parseInt(reader.getAttributeValue(0)));
						case "SwitchMode" -> tuneTower.add(new Pair<>(reader.getAttributeValue(0), Tower.Mode.valueOf(reader.getAttributeValue(1))));
						case "Apply" -> applyEffect.add(new Pair<>(reader.getAttributeValue(0), reader.getAttributeValue(1)));
					}
				}
				else if (eventType == XMLStreamConstants.END_ELEMENT)
				{
					if (reader.getLocalName().equals("Frame"))
						break;
				}
				reader.next();
			}
			return new EventRecord(fid, buildTower, upgradeTower, callWave, removeEnemy, removeProjectile, enemyDamage, damageToBase, tuneTower, sellTower, applyEffect);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			return null;
		}
	}
}
