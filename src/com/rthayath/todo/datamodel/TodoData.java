package com.rthayath.todo.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;

public class TodoData {
    private static TodoData sInstance = new TodoData();
    private ObservableList<TodoItem> mTodoItemList;
    private static final String mFileName = "TodoListItems.txt";
    private DateTimeFormatter mDatetimeFormatter;

    public static TodoData getInstance() {
        return sInstance;
    }

    private TodoData(){
        mDatetimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault());
    }

    public void setTodoItemList(ObservableList<TodoItem> mTodoItemList) {
        this.mTodoItemList = mTodoItemList;
    }

    public ObservableList<TodoItem> getTodoItems() {
        return mTodoItemList;
    }

    public void loadTodoList() throws IOException {
        mTodoItemList = FXCollections.observableArrayList();
        Path path = Paths.get(mFileName);

        BufferedReader bufferedReader = Files.newBufferedReader(path);

        String input;

        try {
            while ((input = bufferedReader.readLine()) != null) {
                String[] itemDes = input.split("\t");

                TodoItem todoItem = new TodoItem(itemDes[0], itemDes[1], LocalDate.parse(itemDes[2], mDatetimeFormatter));
                mTodoItemList.add(todoItem);
            }
        }

        finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

    }

    public void storeData() throws IOException{
        Path path = Paths.get(mFileName);
        BufferedWriter bw = Files.newBufferedWriter(path);

        try {
            Iterator<TodoItem> iter = mTodoItemList.iterator();
            while (iter.hasNext()) {
                TodoItem item = iter.next();
                bw.write(String.format("%s\t%s\t%s",
                        item.getShortDescription(),
                        item.getDetails(),
                        item.getDeadline().format(mDatetimeFormatter)));
                bw.newLine();
            }
        }

        finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    public void addTodoItem(TodoItem todoItem) {
        mTodoItemList.add(todoItem);
    }

    public void deleteItem(TodoItem item) {
        mTodoItemList.remove(item);
    }
}
