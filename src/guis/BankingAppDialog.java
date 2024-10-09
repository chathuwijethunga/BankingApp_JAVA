package guis;

import db_objs.MyJDBC;
import db_objs.Transactions;
import db_objs.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;

public class BankingAppDialog extends JDialog implements ActionListener {
    private User user;
    private BankingAppGui bankingAppGui;
    private JLabel balanceLabel, enterAmountLabel, enterUserLabel;
    private JTextField enterAmountField, enterUserField;
    private JButton actionButton;
    private JPanel pastTransactionPanel;
    private ArrayList<Transactions> pastTransactions;

    public BankingAppDialog(BankingAppGui bankingAppGui, User user){
        // set the size
        setSize(400, 400);

        // add focus to the dialog (can't interact with anything else until dialog is closed)
        setModal(true);

        // loads in teh center of our banking gui
        setLocationRelativeTo(bankingAppGui);

        // when suer closes dialog, it releases its resources that are being used
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // prevents dialog from being resized
        setResizable(false);

        // allows us to manually specify the size and position of each component
        setLayout(null);

        // we will need reference to our gui so that we can update the current balance
        this.bankingAppGui = bankingAppGui;

        // we will need access to the user info to make updates to our db or retrieve data about the user
        this.user = user;
    }
    public void addCurrentBalanceAmount(){
        balanceLabel = new JLabel("Balance: $" + user.getCurrentBalance());
        balanceLabel.setBounds(0,10,getWidth() -20, 20);
        balanceLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(balanceLabel);

        enterAmountLabel = new JLabel("Enter Amount: ");
        enterAmountLabel.setBounds(0,50,getWidth() -20, 20);
        enterAmountLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        enterAmountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterAmountLabel);

        enterAmountField = new JTextField();
        enterAmountField.setBounds(15,80,getWidth() -50, 40);
        enterAmountField.setFont(new Font("Dialog", Font.BOLD, 20));
        enterAmountField.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterAmountField);

    }
    public void addActionButton(String actionButtonType){
        actionButton = new JButton(actionButtonType);
        actionButton.setBounds(15,300,getWidth() -50, 40);
        actionButton.setFont(new Font("Dialog", Font.BOLD, 20));
        actionButton.addActionListener(this);
        add(actionButton);

    }

    public void addUserField(){
        enterUserLabel = new JLabel("Enter User");
        enterUserLabel.setBounds(0,160,getWidth() -20, 20);
        enterUserLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        enterUserLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterUserLabel);

        enterUserField = new JTextField();
        enterUserField.setBounds(15,190,getWidth() -50, 40);
        enterUserField.setFont(new Font("Dialog", Font.BOLD, 20));
        enterUserField.setHorizontalAlignment(SwingConstants.RIGHT);
        add(enterUserField);
    }
    public void addPastTransactionComponents(){
        // container where we will store each transaction
        pastTransactionPanel = new JPanel();

        // make layout 1x1
        pastTransactionPanel.setLayout(new BoxLayout(pastTransactionPanel, BoxLayout.Y_AXIS));

        // add scalability to the container
        JScrollPane scrollPane = new JScrollPane(pastTransactionPanel);

        // displays the vertical scroll only when it is required
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 20, getWidth() - 15, getHeight() - 80);

        // perform db call to retrieve all the past transaction and store into array list
        pastTransactions = MyJDBC.getPastTransaction(user);

        // iterate through the list and add to the gui
        for(int i = 0; i < pastTransactions.size(); i++){
            // store current transaction
            Transactions pastTransaction = pastTransactions.get(i);

            // create a container to store an individual transaction
            JPanel pastTransactionContainer = new JPanel();
            pastTransactionContainer.setLayout(new BorderLayout());

            // create transaction type label
            JLabel transactionTypeLabel = new JLabel(pastTransaction.getTransactionType());
            transactionTypeLabel.setFont(new Font("Dialog", Font.BOLD, 20));

            // create transaction amount label
            JLabel transactionAmountLabel = new JLabel(String.valueOf(pastTransaction.getTransactionAmount()));
            transactionAmountLabel.setFont(new Font("Dialog", Font.BOLD, 20));

            // create transaction date label
            JLabel transactionDateLabel = new JLabel(String.valueOf(pastTransaction.getTransactionDate()));
            transactionDateLabel.setFont(new Font("Dialog", Font.BOLD, 20));

            // add to the container
            pastTransactionContainer.add(transactionTypeLabel, BorderLayout.WEST); // place this on the west side
            pastTransactionContainer.add(transactionAmountLabel, BorderLayout.EAST); // place this on the east side
            pastTransactionContainer.add(transactionDateLabel, BorderLayout.SOUTH); // place this on the south side

            // give a white background to each container
            pastTransactionContainer.setBackground(Color.WHITE);

            // give a black border to each transaction container
            pastTransactionContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // add transaction component to the transaction panel
            pastTransactionPanel.add(pastTransactionContainer);
        }

        // add to the dialog
        add(scrollPane);
    }


    private void handleTransaction(String transactionType, float amountVal){
        Transactions transactions;
        if(transactionType.equalsIgnoreCase("Deposit")){
            user.setCurrentBalance(user.getCurrentBalance().add(new BigDecimal(amountVal)));
            transactions = new Transactions(user.getId(), transactionType, new BigDecimal(amountVal), null);
        }else{
            user.setCurrentBalance(user.getCurrentBalance().subtract(new BigDecimal(amountVal)));

            transactions = new Transactions(user.getId(), transactionType, new BigDecimal(-amountVal), null);

        }
        if(MyJDBC.addTransctionTodatabase(transactions) && MyJDBC.updateCurrentBalance(user)){
            JOptionPane.showMessageDialog(this, transactionType + "Successful!" );

            resetFieldsAndUpdateCurrentBalance();

        }else{
            JOptionPane.showMessageDialog(this, transactionType + "Failed!");
        }
    }
    private void resetFieldsAndUpdateCurrentBalance(){
        enterAmountField.setText("");
        if (enterUserField != null){
            enterUserField.setText("");
        }

        balanceLabel.setText("balance: $" + user.getCurrentBalance());
        bankingAppGui.getCurrentBalanceField().setText("$" + user.getCurrentBalance());
    }
    private void handleTransfer(User user, String transferredUser, float amount ){
        // attempt to perform transfer
        if(MyJDBC.transfer(user, transferredUser, amount)){
            // show success dialog
            JOptionPane.showMessageDialog(this, "Transfer Success!");
            resetFieldsAndUpdateCurrentBalance();
        }else{
            // show failure dialog
            JOptionPane.showMessageDialog(this, "Transfer Failed...");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonPressed = e.getActionCommand();

        float amountVal = Float.parseFloat(enterAmountField.getText());

        if (buttonPressed.equalsIgnoreCase("Deposit")){
            handleTransaction(buttonPressed, amountVal);
        }else{
            int result = user.getCurrentBalance().compareTo(BigDecimal.valueOf(amountVal));
            if(result < 0){
                // display error dialog
                JOptionPane.showMessageDialog(this, "Error: Input value is more than current balance");
                return;
            }
            // check to see if withdraw or transfer was pressed
            if(buttonPressed.equalsIgnoreCase("Withdraw")){
                handleTransaction(buttonPressed, amountVal);
            }else{
                // transfer
                String transferredUser = enterUserField.getText();

                // handle transfer
                handleTransfer(user, transferredUser, amountVal);
            }
        }
    }
}
