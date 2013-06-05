/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import util.Coordinate;
import util.CrowFlyCosts;
import util.Locations;
import util.Neighborhood;
import util.NeighborhoodImpl;
import basics.costs.DefaultVehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.VehicleType;

/**
 * Contains and describes the vehicle routing problem.
 * 
 * <p>A routing problem is defined as jobs, vehicles and costs. 
 * 
 * <p> To construct the problem, use VehicleRoutingProblem.Builder (VehicleRoutingProblem.Builder.newInstance()). 
 * 
 * <p>By default, fleetSize is INFINITE and fleetComposition is HOMOGENEOUS, transport-costs are calculated as euclidean-distance (CrowFlyCosts),
 * and activity-costs are set to DefaultVehicleRoutingActivityCosts which represent hard time-windows (missed time-windows are penalyzed with Double.MAX_VALUE).
 * 
 *  
 * 
 * @author stefan schroeder
 *
 */
public class VehicleRoutingProblem {
	
	/**
	 * Builder to build the routing-problem.
	 * 
	 * @author stefan schroeder
	 *
	 */
	public static class Builder {

		/**
		 * Returns a new instance of this builder.
		 * 
		 * @return builder
		 */
		public static Builder newInstance(){ return new Builder(); }

		private VehicleRoutingTransportCosts transportCosts;
		
		private VehicleRoutingActivityCosts activityCosts = new DefaultVehicleRoutingActivityCosts();

		private Map<String,Job> jobs;

		private Collection<Vehicle> vehicles;

		private Map<String, Coordinate> coordinates;

		private FleetSize fleetSize = FleetSize.INFINITE;

		private FleetComposition fleetComposition = FleetComposition.HOMOGENEOUS;
		
		private Collection<VehicleType> vehicleTypes;

		public Builder() {
			jobs = new HashMap<String, Job>();
			vehicles = new ArrayList<Vehicle>();
			coordinates = new HashMap<String, Coordinate>();
			vehicleTypes = new ArrayList<VehicleImpl.VehicleType>();
		}

		/**
		 * Create a location (i.e. coordinate) and returns the key of the location which is Coordinate.toString().
		 *  
		 * @param x
		 * @param y
		 * @return locationId
		 * @see Coordinate
		 */
		public String createLocation(double x, double y){
			Coordinate coord = new Coordinate(x, y);
			String id = coord.toString();
			if(!coordinates.containsKey(id)){
				coordinates.put(id, coord);
			}
			return id;
		}

		/**
		 * Returns the unmodifiable map of locations (mapped by their id).
		 * 
		 * @return
		 */
		public Map<String,Coordinate> getLocationMap(){
			return Collections.unmodifiableMap(coordinates);
		}

		/**
		 * Returns the locations collected by this builder.
		 * 
		 * <p>Locations are cached when adding a shipment, service, depot, vehicle.
		 * 
		 * @return locations
		 * 
		 */
		public Locations getLocations(){
			return new Locations() {

				@Override
				public Coordinate getCoord(String id) {
					return coordinates.get(id);
				}
			};
		}

		/**
		 * Sets routing costs.
		 * 
		 * @param costs
		 * @return builder
		 * @see VehicleRoutingTransportCosts
		 */
		public Builder setRoutingCost(VehicleRoutingTransportCosts costs){
			this.transportCosts = costs;
			return this;
		}

		/**
		 * Sets the type of fleetSize.
		 * 
		 * <p>FleetSize is either FleetSize.INFINITE or FleetSize.FINITE
		 * 
		 * @param fleetSize
		 * @return
		 */
		public Builder setFleetSize(FleetSize fleetSize){
			this.fleetSize = fleetSize;
//			if(fleetSize.equals(FleetSize.INFINITE)){
//				fleetSizeIsInfinite=true;
//			}
			return this;
		}

		/**
		 * Sets the fleetComposition.
		 * 
		 * <p>FleetComposition is either FleetComposition.HETEROGENEOUS or FleetComposition.HOMOGENEOUS
		 * 
		 * @param fleetComposition
		 * @return
		 */
		public Builder setFleetComposition(FleetComposition fleetComposition){
			this.fleetComposition = fleetComposition;
			return this;
		}

		/**
		 * Adds a service to jobList.
		 * 
		 * <p>If jobList already contains service, a warning message is printed, and the existing job will be overwritten.
		 * 
		 * @param service
		 * @return
		 */
		public Builder addService(Service service){
			coordinates.put(service.getLocationId(), service.getCoord());
			if(jobs.containsKey(service.getId())){ logger.warn("service " + service + " already in job list. overrides existing job."); }
			jobs.put(service.getId(),service);
			return this;
		}

		/**
		 * Adds a job which is either a service or a shipment.
		 * 
		 * @param job
		 * @return
		 * @throws IllegalStateException if job is neither a shipment or a service.
		 */
		public Builder addJob(Job job) {
			if(job instanceof Service) { 
				addService((Service) job);
			}
//			else if(job instanceof Shipment){
//				addShipment((Shipment)job);
//			}
			else throw new IllegalStateException("job can only be a shipment or a service, but is instance of " + job.getClass());
			return this;
		}

//		private void addShipment(Shipment job) {
//			coordinates.put(job.getFromId(),job.getFromCoord());
//			coordinates.put(job.getToId(), job.getToCoord());
//			if(jobs.containsKey(job.getId())){ logger.warn("service " + job + " already in job list. overrides existing job."); }
//			jobs.put(job.getId(),job);
//
//		}

		/**
		 * Adds a vehicle.
		 * 
		 * 
		 * @param vehicle
		 * @return
		 */
		public Builder addVehicle(Vehicle vehicle) {
//			fleetSizeIsFinite = true;
			vehicles.add(vehicle);
			if(!vehicleTypes.contains(vehicle.getType())){
				vehicleTypes.add(vehicle.getType());
			}
			coordinates.put(vehicle.getLocationId(), vehicle.getCoord());
			return this;
		}
		
		/**
		 * Adds a vehicleType.
		 * 
		 * @param type
		 * @return builder
		 */
		public Builder addVehicleType(VehicleType type){
			vehicleTypes.add(type);
			return this;
		}

		
		/**
		 * Sets the activityCostFunction that considers also activities on a vehicle-route.
		 * 
		 * <p>Here you can consider missed time-windows for example. By default, this is set to a DefaultVehicleActivityCostFunction.
		 * 
		 * @param activityCosts
		 * @return
		 * @see VehicleRoutingTransportCosts, DefaultVehicleRouteCostFunction
		 */
		public Builder setActivityCosts(VehicleRoutingActivityCosts activityCosts){
			this.activityCosts = activityCosts;
			return this;
		}

		public VehicleRoutingProblem build() {
			log.info("build problem ...");
			if(transportCosts == null){
				logger.warn("set routing costs crowFlyDistance.");
				transportCosts = new CrowFlyCosts(getLocations());
			}
			
			return new VehicleRoutingProblem(this);
		}

		public Builder addLocation(String id, Coordinate coord) {
			coordinates.put(id, coord);
			return this;
		}

		public Builder addAllJobs(Collection<Job> jobs) {
			for(Job j : jobs){
				addJob(j);
			}
			return this;
		}

		public Builder addAllVehicles(Collection<Vehicle> vehicles) {
			for(Vehicle v : vehicles){
				addVehicle(v);
			}
			return this;
		}
}
	
	/**
	 * Enum that characterizes the fleet-size.
	 * 
	 * @author sschroeder
	 *
	 */
	public static enum FleetSize {
		FINITE, INFINITE;
	}
	
	/**
	 * Enum that characterizes fleet-compostion.
	 * 
	 * @author sschroeder
	 *
	 */
	public static enum FleetComposition {
		HETEROGENEOUS, HOMOGENEOUS;
	}
	
	public static Builder newBuilderInstance(){
		return new Builder();
	}
	
	public static Logger log = Logger.getLogger(VehicleRoutingProblem.class);
	
	private static Logger logger = Logger.getLogger(VehicleRoutingProblem.class);

	private VehicleRoutingTransportCosts transportCosts;
	
	private VehicleRoutingActivityCosts activityCosts;
	
	private Neighborhood neighborhood;
	
	/**
	 * @return the neighborhood
	 */
	public Neighborhood getNeighborhood() {
		return neighborhood;
	}

	/**
	 * @param neighborhood the neighborhood to set
	 */
	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
	}

	private final Map<String, Job> jobs;

	/**
	 * Collection that contains available vehicles.
	 */
	private final Collection<Vehicle> vehicles;
	
	/**
	 * Collection that contains all available types.
	 */
	private Collection<VehicleType> vehicleTypes;
	
	/**
	 * An enum that indicates type of fleetSize. By default, it is INFINTE
	 */
	private FleetSize fleetSize = FleetSize.INFINITE;
	
	/**
	 * An enum that indicates fleetSizeComposition. By default, it is HOMOGENOUS.
	 */
	private FleetComposition fleetComposition;
	

	private VehicleRoutingProblem(Builder builder) {
		this.jobs = builder.jobs;
		this.fleetComposition = builder.fleetComposition;
		this.fleetSize = builder.fleetSize;
		this.vehicles=builder.vehicles;
		this.vehicleTypes = builder.vehicleTypes;
		this.transportCosts = builder.transportCosts;
		this.activityCosts = builder.activityCosts;
		this.neighborhood = new NeighborhoodImpl(this);
		log.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[fleetSize="+fleetSize+"][fleetComposition="+fleetComposition+"][#jobs="+jobs.size()+"][#vehicles="+vehicles.size()+"][#vehicleTypes="+vehicleTypes.size()+"]["+
						"transportCost="+transportCosts+"][activityCosts="+activityCosts+"]";
	}

	/**
	 * Returns fleet-composition.
	 * 
	 * @return fleetComposition which is either FleetComposition.HETEROGENEOUS or FleetComposition.HOMOGENEOUS
	 */
	public FleetComposition getFleetComposition() {
		return fleetComposition;
	}

	public void setFleetComposition(FleetComposition fleetComposition){
		this.fleetComposition = fleetComposition;
	}
	
	/**
	 * Returns type of fleetSize, either INFINITE or FINITE.
	 * 
	 * <p>By default, it is INFINITE.
	 * 
	 * @return either FleetSize.INFINITE or FleetSize.FINITE
	 */
	public FleetSize getFleetSize() {
		return fleetSize;
	}
	
	public void setFleetSize(FleetSize fleetSize){
		this.fleetSize = fleetSize;
	}

	/**
	 * Returns the unmodifiable job map.
	 * 
	 * @return unmodifiable jobMap
	 */
	public Map<String, Job> getJobs() {
		return Collections.unmodifiableMap(jobs);
	}

	
	/**
	 * Returns the entire, unmodifiable collection of types.
	 * 
	 * @return unmodifiable collection of types
	 * @see VehicleType
	 */
	public Collection<VehicleType> getTypes(){
		return Collections.unmodifiableCollection(vehicleTypes);
	}
	
	
	/**
	 * Returns the entire, unmodifiable collection of vehicles.
	 * 
	 * @return unmodifiable collection of vehicles
	 * @see Vehicle
	 */
	public Collection<Vehicle> getVehicles() {
		return Collections.unmodifiableCollection(vehicles);
	}

	/**
	 * Returns routing costs.
	 * 
	 * @return routingCosts
	 * @see VehicleRoutingTransportCosts
	 */
	public VehicleRoutingTransportCosts getTransportCosts() {
		return transportCosts;
	}

	/**
	 * Sets routing costs.
	 * 
	 * @param costs
	 * @see VehicleRoutingTransportCosts
	 */
	public void setTransportCosts(VehicleRoutingTransportCosts costs) {
		this.transportCosts = costs;
		logger.info("transport costs set to " + costs.getClass());
	}
	
	/**
	 * Returns activityCosts.
	 */
	public VehicleRoutingActivityCosts getActivityCosts(){
		return activityCosts;
	}
	
	/**
	 * Sets activityCosts.
	 */
	public void setActivityCosts(VehicleRoutingActivityCosts activityCosts){
		this.activityCosts = activityCosts;
		logger.info("activtiy costs set to " + activityCosts.getClass());
	}
	
}