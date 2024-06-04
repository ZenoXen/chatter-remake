package org.zh.chatter.component;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ActionButtonTableCell<S> extends TableCell<S, Button> {
    private final Button actionButton;
    private final Function<S, Boolean> showFunction;

    public ActionButtonTableCell(String label, BiFunction<S, Button, S> function, Function<S, Boolean> showFunction) {
        this.getStyleClass().add("action-button-table-cell");
        this.actionButton = new Button(label);
        this.actionButton.setOnAction((e) -> function.apply(getCurrentItem(), actionButton));
        this.showFunction = showFunction;
        this.actionButton.setMaxWidth(Double.MAX_VALUE);
    }

    private S getCurrentItem() {
        return getTableRow().getItem();
    }

    public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, BiFunction<S, Button, S> function, Function<S, Boolean> showFunction) {
        return param -> new ActionButtonTableCell<>(label, function, showFunction);
    }

    @Override
    public void updateItem(Button item, boolean empty) {
        super.updateItem(item, empty);
        S currentItem = this.getCurrentItem();
        if (empty || (showFunction != null && !showFunction.apply(currentItem))) {
            setGraphic(null);
        } else {
            setGraphic(actionButton);
        }
    }
}
