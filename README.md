# Hazelcast-VS-EJBTimers
The main goal of this project is to cluster EJB timers with Hazelcast 
and gather common server resources together to from clustered Singleton. 
Also this solution can work with any delayed future tasks ('timers') and not only in EJB container.

There are some facets:
1) Timer should initialized only once and only on one cluster node.
2) All nodes may be equivalents. There is no limitations and hardcode to pin timers to one specific node.
3) In addition to line above, timers should have some privileges on initialization on a chosen nodes during start up.
4) Timers may be initialized in every time: both at application start and after.
5) When a node crashes, timers should migrate to a helthy one.
6) There are should be API to start, stop, delete, init, migrate timers.

Some restrictions:
1) There is no persisted timers yet in terms of EJB.
2) Timers operations are not participate in transactions. 
So if you would start a timer and after that in some code you get exception during single trancsaction 
- timer would not be rollbacked and continue running.

                                                  USE CASES
--------------------------------------------------------------------------------------
Example of initialization Hazelcast:
NetworkConfiguration networkConfiguration = fetchNetConfiguration(); //Fetch cluster nodes relations and other info 
ILocalServiceResolver serviceRouter = new ClusterServiceRouter();
ClusterService clusterService = new ClusterService(serviceRouter);
clusterService.init(networkConfiguration); 

--------------------------------------------------------------------------------------
ClusterService class wraps some Hazelcast logic for distributed computing and release method
IClusterService.getClusterTimerManager() that return Object of type IClusterTimerManager to manage timers.

