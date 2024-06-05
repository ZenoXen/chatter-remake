package org.zh.chatter.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;

import java.util.ArrayList;
import java.util.List;

public class NodeUtil {

    public static <T extends Node> T findFirstNodeByStyleClass(List<Node> nodes, String styleClass, Class<T> clazz) {
        return nodes.stream().filter(n -> n.getStyleClass().stream().anyMatch(s -> s.equals(styleClass))).findAny().map(clazz::cast).orElse(null);
    }

    public static List<Node> getNestedNodesByStyleClass(Parent parent) {
        List<Node> nodes = new ArrayList<>();
        getAllNodes(parent, nodes);
        return nodes;
    }

    private static void getAllNodes(Node node, List<Node> allNodes) {
        allNodes.add(node);
        if (node instanceof Parent parent) {
            if (parent instanceof ScrollPane scrollPane) {
                Node content = scrollPane.getContent();
                if (content != null) {
                    getAllNodes(content, allNodes);
                }
            } else if (parent instanceof ToolBar toolBar) {
                for (Node child : toolBar.getItems()) {
                    getAllNodes(child, allNodes);
                }
            } else {
                for (Node child : parent.getChildrenUnmodifiable()) {
                    getAllNodes(child, allNodes);
                }
            }
        }
    }
}
