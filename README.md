# Hazelcast-clustered-timers
The main goal of this project is to cluster EJB timers with Hazelcast 
and gather common server resources together to from clustered Singleton. 
Also this solution can work with any delayed future tasks ('timers') and not only in EJB container.

There are some facets:
- Timer should initialized only once and only on one cluster node.
- All nodes may be equivalents. There is no limitations and hardcode to pin timers to one specific node.
- In addition to line above, timers should have some privileges on initialization on a chosen nodes during start up.
- Timers may be initialized in every time: both at application start and after.
- When a node crashes, timers should migrate to a helthy one.
- There are should be API to start, stop, delete, init, migrate timers.

Some restrictions:
- There is no persisted timers yet in terms of EJB.
- Timers operations are not participate in transactions. 
So if you would start a timer and after that in some code you get exception during single trancsaction 
timer would not be rollbacked and continue running.

                                                  USE CASES
--------------------------------------------------------------------------------------
Example of initialization Hazelcast:

Fetch cluster nodes relations and other info
NetworkConfiguration networkConfiguration = fetchNetConfiguration()

Implement new ILocalServiceResolver
ILocalServiceResolver serviceRouter = new ...
ClusterService clusterService = new ClusterService(serviceRouter)
clusterService.init(networkConfiguration); 

--------------------------------------------------------------------------------------
ClusterService class wraps some Hazelcast logic for distributed computing and include method
IClusterService.getClusterTimerManager() that return Object of type IClusterTimerManager to manage timers.

