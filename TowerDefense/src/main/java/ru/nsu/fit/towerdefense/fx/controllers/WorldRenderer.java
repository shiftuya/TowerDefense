package ru.nsu.fit.towerdefense.fx.controllers;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import ru.nsu.fit.towerdefense.fx.Images;
import ru.nsu.fit.towerdefense.fx.exceptions.RenderException;
import ru.nsu.fit.towerdefense.model.world.gameobject.Renderable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * WorldRenderer class is used for rendering game objects on the game scene.
 *
 * @author Oleg Markelov
 */
public class WorldRenderer {

    private final ObservableList<Node> gameNodes;
    private final double pixelsPerGameCell;

    private final Map<Renderable, Node> renderableToGameNodeMap = new HashMap<>();

    /**
     * Creates new WorldRenderer with specified game nodes.
     *
     * @param gameNodes         list of game nodes.
     * @param pixelsPerGameCell how many pixels fit one dimension of game cell.
     */
    public WorldRenderer(ObservableList<Node> gameNodes, double pixelsPerGameCell) {
        this.gameNodes = gameNodes;
        this.pixelsPerGameCell = pixelsPerGameCell;
    }

    /**
     * Updates a map (Renderable -> game node) with new renderables.
     *
     * @param newRenderableSet updated renderables.
     */
    public void update(Set<Renderable> newRenderableSet) throws RenderException {
        for (Renderable renderable : newRenderableSet) {
            if (!renderableToGameNodeMap.containsKey(renderable)) {
                try {
                    ImageView imageView =
                        new ImageView(Images.getInstance().getImage(renderable.getImageName()));

                    imageView.setOnMouseClicked(mouseEvent -> System.out.println(renderable.getImageName()));

                    imageView.setUserData(renderable);
                    renderableToGameNodeMap.put(renderable, imageView);
                } catch (NoSuchElementException e) {
                    throw new RenderException("No image was found by name \"" + renderable.getImageName() + "\".");
                }
            }
        }

        renderableToGameNodeMap.entrySet().removeIf(entry -> !newRenderableSet.contains(entry.getKey()));
    }

    /**
     * Renders previously updated renderables.
     *
     * Must be called in JavaFX Application thread!
     */
    public void render() {
        gameNodes.removeIf(node -> !renderableToGameNodeMap.containsKey(node.getUserData()));

        for (Map.Entry<Renderable, Node> entry : new ArrayList<>(renderableToGameNodeMap.entrySet())) {
            Renderable renderable = entry.getKey();
            ImageView imageView = (ImageView) entry.getValue();

            imageView.relocate(
                renderable.getPosition().getX() * pixelsPerGameCell,
                renderable.getPosition().getY() * pixelsPerGameCell);

            imageView.setFitWidth(renderable.getSize().getX() * pixelsPerGameCell);
            imageView.setFitHeight(renderable.getSize().getY() * pixelsPerGameCell);

            imageView.setRotate(renderable.getRotation());
            imageView.setViewOrder(-renderable.getZIndex());

            if (!gameNodes.contains(imageView)) {
                gameNodes.add(imageView);
            }
        }
    }
}
