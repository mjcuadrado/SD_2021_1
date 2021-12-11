/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipes_service.tsae.data_structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import recipes_service.data.Operation;
//LSim logging system imports sgeag@2017
//import lsim.coordinator.LSimCoordinator;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.library.api.LSimLogger;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class Log implements Serializable{
	// Only for the zip file with the correct solution of phase1.Needed for the logging system for the phase1. sgeag_2018p 
//	private transient LSimCoordinator lsim = LSimFactory.getCoordinatorInstance();
	// Needed for the logging system sgeag@2017
//	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -4864990265268259700L;
	/**
	 * This class implements a log, that stores the operations
	 * received  by a client.
	 * They are stored in a ConcurrentHashMap (a hash table),
	 * that stores a list of operations for each member of 
	 * the group.
	 */
	private ConcurrentHashMap<String, List<Operation>> log= new ConcurrentHashMap<String, List<Operation>>();  

	public Log(List<String> participants){
		// create an empty log
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			log.put(it.next(), new Vector<Operation>());
		}
	}

	/**
	 * inserts an operation into the log. Operations are 
	 * inserted in order. If the last operation for 
	 * the user is not the previous operation than the one 
	 * being inserted, the insertion will fail.
	 * 
	 * @param op
	 * @return true if op is inserted, false otherwise.
	 */
	public synchronized boolean add(Operation op){
	
		 String hostId = op.getTimestamp().getHostid();
		 //Ultimo timestamp por host
	     Timestamp lastTHost = this.getLastTimestamp(hostId);
	    //Diferencia con el guardado
        long difference = op.getTimestamp().compare(lastTHost);

        //comprobafinoes
        if ((lastTHost == null && difference == 0)
                || (lastTHost != null && difference == 1)) {
        	//Lo añadimos
            this.log.get(hostId).add(op);
            return true;
        } else {
            return false;
        }
	}
	
	/**
	 * Checks the received summary (sum) and determines the operations
	 * contained in the log that have not been seen by
	 * the proprietary of the summary.
	 * Returns them in an ordered list.
	 * @param sum
	 * @return list of operations
	 */
	public synchronized List<Operation> listNewer(TimestampVector sum){

		 List<Operation> missingOperations = new ArrayList();

		 	//Recorremos los hos
	        for (String host : this.log.keySet()) {
	        	
	        	//Cogemos los datos del host
	            List<Operation> operations = this.log.get(host);
	            Timestamp timestampHost = sum.getLast(host);

	            //Recorremos las operaciones que tiene este host
	            for (Operation op : operations) {
	                if (op.getTimestamp().compare(timestampHost) > 0) {
	                	//No tenemos esta operacion
	                    missingOperations.add(op);
	                }
	            }
	        }
	        
	        //Devolvemos las operaciones que no tenemos
	        return missingOperations;
	}
	
	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided
	 * ackSummary. 
	 * @param ack: ackSummary.
	 */
	public void purgeLog(TimestampMatrix ack){
	}

	/**
	 * equals
	 */
	@Override
	public boolean equals(Object obj) {
		/* 
		 * Resultado de la comparación 
		 * Solo nos interesa si el objeto es el mismo
		 * */
		
		//Por defecto devolvemos false
		boolean result= false;
		
		if (obj != null && obj == this) {
			result = true;
		}else if (obj != null) {
			Log parameterLog = (Log) obj;
			result = log.equals(parameterLog.log);
		}
		
		return result;
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String name="";
		for(Enumeration<List<Operation>> en=log.elements();
		en.hasMoreElements(); ){
		List<Operation> sublog=en.nextElement();
		for(ListIterator<Operation> en2=sublog.listIterator(); en2.hasNext();){
			name+=en2.next().toString()+"\n";
		}
	}
		
		return name;
	}
	
	//Devuelve el host Id de la operación
	private String getHostID(Operation op) {
		return op.getTimestamp().getHostid();
	}
	
	//Devuelve el log de lista de operaciones por key (hostID)
	private List<Operation> getOperationsByHost(String key) {
		return this.log.get(key);
	}
	
	//Obtiene la última operación de la lista. Si no hay devuelve null
	private Operation getLastOperation(List<Operation> logOperations) {
		return (logOperations.size() > 0) ? logOperations.get(logOperations.size()-1) : null;
	}
	
	//Obtiene el último Timestamp por Log
	private Timestamp getLastTimestamp(String hostId) {
        List<Operation> operations = this.log.get(hostId);

        if (operations == null || operations.isEmpty()) {
            return null;
        } else {
            return operations.get(operations.size() - 1).getTimestamp();
        }
    }
}
