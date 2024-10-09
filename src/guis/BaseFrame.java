package guis;

import db_objs.User;

import javax.swing.*;

public abstract class BaseFrame extends JFrame {
    //store user info
    protected User user;

    public BaseFrame(String title) {initialize(title);}
    public BaseFrame(String title, User user){
        //initialize user
        this.user = user;

        initialize(title);
    }
    private void initialize(String title){
        setTitle(title);
        setSize(420, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        addGuiComponents();
    }
    protected abstract void addGuiComponents();
}
