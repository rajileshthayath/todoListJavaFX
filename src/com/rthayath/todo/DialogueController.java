package com.rthayath.todo;

import com.rthayath.todo.datamodel.TodoData;
import com.rthayath.todo.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogueController {

    @FXML
    private TextField shortDescription;

    @FXML
    private TextArea detailsText;

    @FXML
    private DatePicker dueDatePicker;


    TodoItem processData() {
        String shortDesc = shortDescription.getText().trim();
        String details = detailsText.getText().trim();
        LocalDate localDate = dueDatePicker.getValue();

        TodoItem todoItem = new TodoItem(shortDesc, details, localDate);

        TodoData.getInstance().addTodoItem(todoItem);
        return todoItem;
    }

    void onEditTodoItem(TodoItem item) {
        shortDescription.setText(item.getShortDescription());
        detailsText.setText(item.getDetails());
        dueDatePicker.setValue(item.getDeadline());
    }
}
