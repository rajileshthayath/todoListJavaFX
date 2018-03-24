package com.rthayath.todo;

import com.rthayath.todo.datamodel.TodoData;
import com.rthayath.todo.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    @FXML
    private BorderPane mainBoarderPane;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadLineLabel;
    @FXML
    private ListView<TodoItem> todoListview;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton todayToggleButton;

    private FilteredList<TodoItem> mFilteredList;
    private Predicate<TodoItem> mAllItems;
    private Predicate<TodoItem> mTodaysItems;

    public void initialize() {

        try {
            TodoData.getInstance().loadTodoList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listContextMenu = new ContextMenu();

        initMenuOps();

        todoListview.getSelectionModel().selectedItemProperty().addListener(mItemChangeListener);
//        todoListview.setItems(TodoData.getInstance().getTodoItems());//todoItems
        todoListview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListview.getSelectionModel().selectFirst();
        todoListview.setCellFactory(param -> {
            ListCell<TodoItem> cell = new ListCell<TodoItem>() {
                @Override
                protected void updateItem(TodoItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    }
                    else {
                        setText(item.getShortDescription());
                        setTextFill(Color.BLACK);
                        if (item.getDeadline().equals(LocalDate.now())) {
                            setTextFill(Color.RED);
                        }
                    }
                }
            };

            cell.emptyProperty().addListener(
                    (observable, wasEmpty, isNowEmpty) -> {
                        if (isNowEmpty) {
                            cell.setContextMenu(null);
                        }
                        else {
                            cell.setContextMenu(listContextMenu);
                        }

            });

            return cell;
        });

        initMethods();

        mFilteredList = new FilteredList<>(TodoData.getInstance().getTodoItems(), item -> true);
        SortedList<TodoItem> sortedList = new SortedList<>(mFilteredList, Comparator.comparing(TodoItem::getDeadline));
        todoListview.setItems(sortedList);
    }

    private void initMenuOps() {
        MenuItem delMenu = new MenuItem("Delete");
        MenuItem editMenu = new MenuItem("Edit");

        delMenu.setOnAction(event -> {
            TodoItem item = todoListview.getSelectionModel().getSelectedItem();
            deleteItem(item);
        });

        editMenu.setOnAction(event -> {
            TodoItem item = todoListview.getSelectionModel().getSelectedItem();
            showNewDialog(item);
        });

        listContextMenu.getItems().addAll(delMenu);
        listContextMenu.getItems().addAll(editMenu);
    }

    public void handleOnKeyPressed(KeyEvent keyEvent) {

        if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
            TodoItem item = todoListview.getSelectionModel().getSelectedItem();
            if (item != null) {
                deleteItem(item);
            }
        }
    }

    public void showNewDialog() {
        showNewDialog(null);
    }

    public void showNewDialog(TodoItem item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBoarderPane.getScene().getWindow());
        dialog.setTitle("Add new TODO");
        dialog.setHeaderText("Add details below.");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todo_item_dialogue.fxml"));
        try {

            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        DialogueController controller = fxmlLoader.getController();

        if (item != null) {
            controller.onEditTodoItem(item);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        }
        else {
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        }

        Optional<ButtonType> result = dialog.showAndWait();

        if (result == null || !result.isPresent()) {
            return;
        }

        if (result.get() == ButtonType.OK) {
            //ok pressed

            TodoItem itemAdded = controller.processData();
            todoListview.getSelectionModel().select(itemAdded);
        }
        else if (result.get() == ButtonType.APPLY){
            //edit and apply
            TodoData.getInstance().deleteItem(item);
            TodoItem itemAdded = controller.processData();
            todoListview.getSelectionModel().select(itemAdded);

        }
        else {
            //cancel
        }
    }

    /*public void handleSelectedListItem() {
        TodoItem selected = todoListview.getSelectionModel().getSelectedItem();
        *//*itemDetails.setText(selected.getShortDescription() + ", " +
                            selected.getDetails() + ", "+
                            selected.getDeadline());*//*

        *//*StringBuilder sb = new StringBuilder(selected.getDetails());
        sb.append("\n");
        sb.append("Due ");
        sb.append(selected.getDeadline());
        itemDetails.setText(sb.toString());*//*

        itemDetailsTextArea.setText(selected.getDetails());
        deadLineLabel.setText(selected.getDeadline().toString());
    }*/

    private ChangeListener<TodoItem> mItemChangeListener = (observable, oldValue, newValue) -> {
        TodoItem selectedItem = todoListview.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            itemDetailsTextArea.setText(selectedItem.getDetails());
            deadLineLabel.setText(selectedItem.getDeadline().toString());
        }
    };

    private void deleteItem(TodoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete Item?");
        alert.setContentText("Do you want to delete the item? Press OK to confirm");
        Optional<ButtonType> result = alert.showAndWait();
         if (result != null && result.get() == ButtonType.OK) {
             //confirm
             TodoData.getInstance().deleteItem(item);
         }
    }

    @FXML
    public void filterTodaysItems() {
        TodoItem item = todoListview.getSelectionModel().getSelectedItem();
        if (todayToggleButton.isSelected()) {
            mFilteredList.setPredicate(mTodaysItems);

            if (mFilteredList.contains(item)) {
                todoListview.getSelectionModel().select(item);
            }
            else {
                todoListview.getSelectionModel().selectFirst();
            }
        }
        else {
            mFilteredList.setPredicate(mAllItems);
            todoListview.getSelectionModel().select(item);
        }
    }

    private void initMethods() {
        mAllItems = (item -> true);
        mTodaysItems = item -> item.getDeadline().equals(LocalDate.now());
    }

    @FXML
    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(mainBoarderPane.getScene().getWindow());
        if (file != null) {
            TodoItem item = new TodoItem(file.getName(), file.getAbsolutePath(), LocalDate.now());
            TodoData.getInstance().addTodoItem(item);
        }
    }

    @FXML
    private void handleExitApp() {
        Platform.exit();
    }

}
