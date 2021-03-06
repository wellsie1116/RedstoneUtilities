/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.kevinwells.redstoneutilities;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;


/**
 *
 * @author Tal Eisenberg
 */
public class deathdoor extends Circuit {

	private int code[];
	private int codeIndex = 0;
	private boolean codeCorrect = true;
	private boolean failed = false;
	
	private static int INPUT_PRESSURE = 0;
	//private static int INPUT_BTN1 = 1;
	//private static int INPUT_BTN2 = 2;
	//private static int INPUT_BTN3 = 3;
	
	private static int OUTPUT_FLOOR = 0;
	private static int OUTPUT_DOOR = 1;
	private static int OUTPUT_FAILED = 2;
	
	
	private void sendDelayedOutput(final int index, final boolean state, long millis) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!inputBits.get(0))
					sendOutput(index, state);
			}
		}, millis);
	}
	
    @Override
    public void inputChange(int index, boolean state) {
    	//inputs
    	// pressure
    	// btn1
    	// btn2
    	// btn3

    	//outputs
    	// doors
    	// failed
    	// floor
    	
    	if (index == INPUT_PRESSURE) {
    		if (state) {
    			if (hasDebuggers()) debug("Manually opening door");
    			sendOutput(OUTPUT_DOOR, true);
    			if (failed) {
    				if (hasDebuggers()) debug("Clearing failed state");
    				failed = false;
    				sendOutput(OUTPUT_FAILED, false);
    			}
    		} else {
    			if (hasDebuggers()) debug("Closing door in 1 second");
    			sendDelayedOutput(OUTPUT_DOOR, false, 1000);
    		}
    	} else {
    		if (state) {
    			if (code[codeIndex] != index)
    				codeCorrect = false;
    			codeIndex++;
    			if (codeIndex == code.length) {
    				if (codeCorrect) {
    					if (failed) {
    						failed = false;
    						sendOutput(OUTPUT_FAILED, false);
    					}
    					sendOutput(OUTPUT_DOOR, true);
    					if (hasDebuggers()) debug("Code correct: opening door");
    					
    				} else if (!failed) {
    					sendOutput(OUTPUT_FAILED, true);
    					failed = true;
    					if (hasDebuggers()) debug("Code incorrect: try one more time");
    				} else {
    					sendOutput(OUTPUT_FLOOR, true);
    					sendDelayedOutput(OUTPUT_FLOOR, false, 2000);
    					if (hasDebuggers()) debug("Code incorrect: goodbye");
    				}
    				codeIndex = 0;
    				codeCorrect = true;
    			}
    		}
    	}
    }

    @Override
    protected boolean init(CommandSender sender, String[] strings) {
        // This code executes when you right-click on the circuit's sign and again each
        // time the the server is restarted or the RedstoneChips plugin is enabled.

        info(sender, "You have activated deathdoor test circuit.");

        if (strings.length > 0) {
	        String password = strings[0];
	        code = new int[password.length()];
	        
	        for (int i = 0; i < password.length(); i++) {
	        	if (!Character.isDigit(password.charAt(i))) {
	        		error(sender, "Invalid password format");
	        		return false;
	        	}
	        	code[i] = Integer.parseInt(String.valueOf(password.charAt(i)));
	        }
        } else {
        	code = new int[] {2, 1, 3};
        }

        return true;
    }

}
