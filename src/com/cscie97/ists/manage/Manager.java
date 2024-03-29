package com.cscie97.ists.manage;

import com.cscie97.ists.authentication.StoreAuthenticationService;
import com.cscie97.ists.customer.CustomerService;
import com.cscie97.ists.customer.Flight;
import com.cscie97.ists.resource.Entity;
import com.cscie97.ists.resource.Observer;
import com.cscie97.ists.resource.ResourceManagementService;
import com.cscie97.ists.resource.Spaceship;
import com.cscie97.ists.resource.Subject;
import com.cscie97.ists.resource.Team;
import com.cscie97.ists.resource.UpdateEvent;
import com.cscie97.ists.manage.Action;

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
    public Flight defineFlight(String id, String number, String spaceshipId, String time, String location, String destination, String duration, Integer numStops
            , Integer capacity, String crewId, Integer ticketPrice, Integer passengerCount, AuthTokenTuple authTokenTuple)
    {        
        // Get spaceship
        LinkedHashMap<String, Spaceship> spaceships = resourceImpl.getSpaceships(new AuthTokenTuple(myAuthToken));
        Spaceship spaceship = spaceships.get(spaceshipId);
        
        // TODO: Check that spaceship isn't scheduled at the time requested?
        // for each flight
        //      if (time + duration overlaps flight.time + flight.duration) && (spaceship == flight.getSpaceship)
        //              throw new Exception
                        
        // Get team
        LinkedHashMap<String, Entity> entities = resourceImpl.getEntities(new AuthTokenTuple(myAuthToken));
        Team team = (Team) entities.get(crewId);
        
        // TODO: Check that crew isn't scheduled at the time requested?
        // for each flight
        //      if (time + duration overlaps flight.time + flight.duration) && (spaceship == flight.getSpaceship)
        //              throw new Exception
                
        // Create new flight
        Flight flight = new Flight(id, number, spaceship, time, location, destination, duration, numStops
                , capacity, team, ticketPrice, passengerCount);
        
        // Add flight to flights list in CustomerImpl
        LinkedHashMap<String, Flight> flights = customerImpl.getFlights(new AuthTokenTuple(myAuthToken));
        flights.put(id, flight);
        
        return flight;
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
            Action statusUpdate = new StatusUpdateCommand(event.getSourceDevice(), "status string");            
            
            // Run the Command's execute method
            statusUpdate.execute();
        }
        
        if (eventString.equals("emergency"))
        {        
            // TODO
            
            // Check if event event is valid/recognizable?
            
            // Create new Emergency               
            Action emergency = new EmergencyCommand(event.getSourceDevice());            
            
            // Run the Command's execute method
            emergency.execute();
        }        
        
        if (eventString.equals("Test ing"))
        {
            Action testCommand = new TestCommand(event.getSourceDevice());
            testCommand.execute();
        }
    }
    
    /* Nested classes */
    
    public class EmergencyCommand extends Action
    {      
        /* Variables */        
        
        String emergencyType;
        
        public EmergencyCommand(Spaceship sourceDevice)
        {
            super(sourceDevice);            
        }

        public void execute()
        {
            /* *
             * Scratch:
             * Actors -- Flight Management service, Communication system, Spacecraft/crew, Auth service, passengers (maybe in case of emergency contact
             * that's in Customer service)
             */
            
            // Get spaceship's location
            String coordinates = sourceDevice.coordinates;   
            
            /* *
             * Check for available spaceships for flight
             *  - Need to be sure enough capacity to fit/rescue passengers?
             *  - Can check ships that are near coordinates for faster rescue
             */
            customerImpl.getFlights(new AuthTokenTuple(myAuthToken)); // See which of the ships can be scheduled
            resourceImpl.getSpaceships(new AuthTokenTuple(myAuthToken)); // See what ships ISTS possesses            
            
            // Check for crew that can be scheduled
            
            // Check which launchpad can be used for flight
            
            // Check which crew can be scheduled for flight
            resourceImpl.getEntitiesVisitor(new AuthTokenTuple(myAuthToken));
            
            // Define the rescue flight to leave for asap
            Flight flight = defineFlight(null, null, null, null, null, null, null, null, null, null, null, null, new AuthTokenTuple(myAuthToken));
            
            // Book crew for the flight (in Customer service)
            
            // Message distressed spaceship that message was received and when to expect the rescue plane (to Communication System)
            
            // Fill spaceship up with fuel?            
        }            
    }
    
    public class TestCommand extends Action
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
    
    public class StatusUpdateCommand extends Action
    {      
        /* Variables */
        
        String status;
        
        public StatusUpdateCommand(Spaceship sourceDevice, String status)
        {
            super(sourceDevice);
            
            this.status = status;
        }

        public void execute()
        {
            // Change the flight's status            
            String currentFlightId = sourceDevice.getCurrentFlightId();
            LinkedHashMap<String, Flight> flights = customerImpl.getFlights(null);
            Flight flight = flights.get(currentFlightId);
            flight.setStatus(status, new AuthTokenTuple(myAuthToken));
            
            // If status update was "Reached destination" then also do ReachedDestinationCommand            
            if (status.equals("reached destination"))
            {             
                Action reachedDestination = new ReachedDestinationCommand(sourceDevice);            
                
                // Run the Command's execute method
                reachedDestination.execute();
            }
        }            
    }
    
    public class ReachedDestinationCommand extends Action
    {      
        /* Variables */
        
        
        
        public ReachedDestinationCommand(Spaceship sourceDevice)
        {
            super(sourceDevice);            
        }

        public void execute()
        {
            // TODO
            
            // Call Location Update command
            LocationUpdateCommand locationUpdate = new LocationUpdateCommand(sourceDevice, 0, "trajectory", "destinationCoordinates");
            locationUpdate.execute();
            
            // Push to IPFS
            customerImpl.pullFromIpfsRepo(new AuthTokenTuple(myAuthToken));
            customerImpl.pushToIpfsRepo(new AuthTokenTuple(myAuthToken));
        }            
    }
    
    public class LocationUpdateCommand extends Action
    {      
        /* Variables */        
        
        Integer speed;
        String trajectory;
        String coordinates;
        
        public LocationUpdateCommand(Spaceship sourceDevice, Integer speed, String trajectory, String coordinates)
        {
            super(sourceDevice);            
        }

        public void execute()
        {
            sourceDevice.setCurrentSpeed(speed, new AuthTokenTuple(myAuthToken));
            sourceDevice.setTrajectory(trajectory, new AuthTokenTuple(myAuthToken));
            sourceDevice.setCoordinates(coordinates, new AuthTokenTuple(myAuthToken));
        }            
    }    
}
