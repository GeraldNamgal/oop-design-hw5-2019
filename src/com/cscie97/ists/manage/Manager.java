package com.cscie97.ists.manage;

import com.cscie97.ists.authentication.StoreAuthenticationService;
import com.cscie97.ists.customer.CustomerService;
import com.cscie97.ists.resource.Observer;
import com.cscie97.ists.resource.ResourceManagementService;
import com.cscie97.ists.resource.Spaceship;
import com.cscie97.ists.resource.Subject;
import com.cscie97.ists.resource.UpdateEvent;
import com.cscie97.ists.manage.Command;

import java.util.LinkedHashMap;

import com.cscie97.ists.authentication.AuthToken;
import com.cscie97.ists.authentication.AuthTokenTuple;


public class Manager implements Observer, FlightManagementService {

    /* Constructor */ 
    
    ResourceManagementService resourceImpl;
    CustomerService customerImpl;
    StoreAuthenticationService authenticator;
    AuthToken myAuthToken;    

    public Manager(Subject resourceImpl, CustomerService customerImpl, StoreAuthenticationService authenticator)
    {       
        // Register Controller with Model Service
        resourceImpl.registerObserver(this);
        
        this.resourceImpl = (ResourceManagementService) resourceImpl;
        this.customerImpl = customerImpl;
        this.authenticator = authenticator;
        
        // Login
        myAuthToken = null;
    }
    
    @Override
    public Flight defineFlight(String id, String number, String time, String location, String destination, String duration, Integer numStops
            , Integer capacity, String crewId, Integer ticketPrice, Integer passengerCount, AuthTokenTuple authTokenTuple)
    {
        // Find available spaceships
        resourceImpl.getSpaceships(new AuthTokenTuple(myAuthToken));
        
        // If no available spaceships, create one
        Spaceship spaceship = null;
        
        // Check flights list for availabilities for booking
        
        // Create new flight
        Flight flight = new Flight(id, number, spaceship, time, location, destination, duration, numStops
                , capacity, crewId, ticketPrice, passengerCount);
        
        // Add flight to flights list in CustomerImpl
        customerImpl.getFlights(new AuthTokenTuple(myAuthToken)).put(id, flight);
        
        return flight;
    }   
    
    @Override
    public LinkedHashMap<String, Flight> getFlights(AuthTokenTuple authTokenTuple) {
        
        return customerImpl.getFlights(new AuthTokenTuple(myAuthToken));
    }
    
    @Override
    public void update(UpdateEvent event)
    {        
        handleEvent(event);
    }

    public void handleEvent(UpdateEvent event)
    {
        // Get event's string array
        String[] eventStrArr = event.getPerceivedEvent();
        
        String eventString = "";
        for (String string : eventStrArr)
        {
            eventString += string + " ";
        }
        
        eventString = eventString.trim();
        
        if (eventString.equals("status update"))
        {        
            // Create new Emergency               
            Command statusUpdate = new StatusUpdateCommand(event.getSourceDevice());            
            
            // Run the Command's execute method
            statusUpdate.execute();
        }
        
        if (eventString.equals("emergency"))
        {        
            // Create new Emergency               
            Command emergency = new EmergencyCommand(event.getSourceDevice());            
            
            // Run the Command's execute method
            emergency.execute();
        }
        
        if (eventString.equals("reached destination"))
        {        
            // Create new Emergency               
            Command reachedDestination = new ReachedDestinationCommand(event.getSourceDevice());            
            
            // Run the Command's execute method
            reachedDestination.execute();
        }
        
        if (eventString.equals("Test ing"))
        {
            Command testCommand = new TestCommand(event.getSourceDevice());
            testCommand.execute();
        }
    }
    
    /* Nested classes */
    
    public class TestCommand extends Command
    {      
        /* Variables */        
        
        
        
        public TestCommand(Spaceship sourceDevice)
        {
            super(sourceDevice);            
        }

        public void execute()
        {
            
        }            
    }
    
    public class StatusUpdateCommand extends Command
    {      
        /* Variables */
        
        String status;
        
        public StatusUpdateCommand(Spaceship sourceDevice)
        {
            super(sourceDevice);            
        }

        public void execute()
        {
            // Change the ship's status
            sourceDevice.status = status;
            
            // TODO: If status update was "Reached destination" then also do ReachedDestinationCommand
        }            
    }
    
    public class EmergencyCommand extends Command
    {      
        /* Variables */        
        
        String emergencyType;
        
        public EmergencyCommand(Spaceship sourceDevice)
        {
            super(sourceDevice);            
        }

        public void execute()
        {
            // Get spaceship's location
            String coordinates = sourceDevice.coordinates;
            
            // Define rescue flight to send
            resourceImpl.defineSpaceship(null, null, null, null, null, null, null, null, null, null);
            Flight flight = defineFlight(null, null, null, null, null, null, null, null, null, null, null, null);
        }            
    }
    
    public class ReachedDestinationCommand extends Command
    {      
        /* Variables */
        
        
        
        public ReachedDestinationCommand(Spaceship sourceDevice)
        {
            super(sourceDevice);            
        }

        public void execute()
        {
            // TODO
            
            customerImpl.pullFromIpfsRepo(customerImpl.getIpnsKeyName(new AuthTokenTuple(myAuthToken)), new AuthTokenTuple(myAuthToken));
            customerImpl.pushToIpfsRepo(customerImpl.getIpnsKeyName(new AuthTokenTuple(myAuthToken)), new AuthTokenTuple(myAuthToken));
        }            
    }
}
