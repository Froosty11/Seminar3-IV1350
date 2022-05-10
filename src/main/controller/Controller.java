package main.controller;

import main.dtos.SaleDTO;
import main.dtos.StoreDTO;
import main.integration.ItemNotFoundException;
import main.integration.AccountingSystem;
import main.integration.ExternalInventorySystem;
import main.integration.ReceiptPrinter;
import main.model.Observer;
import main.model.Receipt;
import main.model.Sale;
import main.model.ItemScanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class controller.
 * It controls a sale when called to.
 * It also contains and handles all databases, and integration with external systems
 *
 */
public class Controller {
    private Sale currentActive;
    private ExternalInventorySystem ext;
    private AccountingSystem act;
    private ReceiptPrinter rp;
    private StoreDTO store;
    private List<Observer> observerList = new ArrayList<Observer>();
    private ItemScanner scnr;

    /**
    *
    * Pre-made Constructor for a controller. Has hardcoded calls for address, telephone number and name.
    *
    * */
    public Controller() {
        store = new StoreDTO("ICA NÄRA", "Björkvägen 22", "0733533596");
        ext = new ExternalInventorySystem();
        act = new AccountingSystem();
        rp = new ReceiptPrinter();
        scnr = new ItemScanner(ext);
    }
    /**
    * Constructor with the actual parameters. Not used anywhere currently.
    * @param name The name of the butik
    * @param address The address, for example kungsgatan 2
    * @param nmr the telephone nummer. make something up
    * */
    public Controller(String name, String address, String nmr){
        store = new StoreDTO(name, address, nmr);
        ext = new ExternalInventorySystem();
        act = new AccountingSystem();
        rp = new ReceiptPrinter();
        scnr = new ItemScanner(ext);

    }

    /**
    * Starts a new sale. Basically runs the constructor for a sale, with some underlying structure too
    * @param customerID the id of the customer. Not necessary atm, but would be needed for discounts.
    * */
    public boolean startNewSale(int customerID) {
        currentActive = new Sale();
        return true;
    }
    /**
    * Adds one item to the currentSale.
    * @param itemID the itemID of the item that should be added*/
    public boolean addItem(int itemID) {
        try{
            return scnr.addItemFromBarcode(itemID, currentActive, 1);
        }
        catch (
                IOException fileNot
        ){
            fileNot.printStackTrace();
        }
        return false;
    }
/**
 * Ends the current sale. Adds the sale to previously done sales in ACT. Prints a receipt using RP.
 * @param paid the amount of money paid. If it'cashier less then the total cost of the sale, returns false
 * @param cashier The cashier who handled the sale
 * @param pos The position of the transaction
 *
 * */
    public boolean endSale(double paid, String cashier, String pos) {
        SaleDTO dto = currentActive.endSale(cashier, pos);
        if (paid > dto.getTotal() + dto.getTotalVAT()) {
            Receipt r = new Receipt(dto, store);
            System.out.print(paid - dto.getTotal() - dto.getTotalVAT() + " change\n ");
            rp.PrintReceipt(r);
            act.registerSale(dto);
            return true;
        }
        return false;

    }
    /**
     * Terminates the sale. Does not store any information
     * */

    public void terminate() {
        currentActive.terminateSale();
        currentActive = null;
    }

    /**
     *
     * @param itemID the itemID of the added item
     * @param count the amount of items added
     * @return only returns true currently* should defintely check through each one.
     */
    public boolean addItem(int itemID, int count)  {
        try{
            scnr.addItemFromBarcode(itemID, currentActive, count);
        }
        catch (IOException fileNot){
            fileNot.printStackTrace();
        }

        return true;
    }

/**
 * Gets string- the current sales sale.string
 * @return a string that represents the current controller
 * */
    public String getString() {
        if (currentActive.getProgress()) {
            String str;
            str = currentActive.toString();
            System.out.println(str);


            return str;
        }
        return null;

    }


}