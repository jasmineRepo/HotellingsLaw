# Hotellings Law

Hotelling (1929) studied competition between firms, taking into account their geographical location. A detailed description of the original model can be find [here](http://ccl.northwestern.edu/netlogo/models/Hotelling'sLaw).

This application of Hotelling’s model simplifies it by only considering changes in location, assuming identical prices across firms and constant price over time. Following Ottino, Stonedahl, and Wilensky (2009), it however extends the original paper by allowing stores to move along a plane (two-dimension movement). 

One difference between their representation and ours is the way distance between consumers and firms is defined. In their model, one unit of distance corresponds to the four cardinal directions around a given coordinate. Here, we also included the patches at the north-west, north-east, south-east and south-west (see ReadMe.pdf). 

The timing of event is as follow: in each period,

1. households locate which stores is currently the closest, 
2. firms know their current revenue, that is the number of consumers if they remain in the same spot (1), and compute the revenue they would have if they were to move to the eight patches around — assuming the other stores remain at their current place. It eventually moves to the location that yields the highest revenue,
3. households consume based on the final location of the stores (demand is perfectly inelastic). 

## Results

As in the original model, a stable equilibrium exists only for duopoly, in which case it is located at the middle of the plane. For all number of stores greater than two, firms keep changing their location. Occasionally, when two firms bump into each other, unstable equilibria emerge, with these two (or more) firms following each other for a while, before eventually separating. 

## References 

* Hotelling, Harold. (1929). "Stability in Competition." The Economic Journal 39.153: 41 -57. (Stable URL: http://www.jstor.org/stable/2224214 ).

* Ottino, B., Stonedahl, F. and Wilensky, U. (2009). NetLogo Hotelling's Law model. http://ccl.northwestern.edu/netlogo/models/Hotelling'sLaw. Center for Connected Learning and Computer-Based Modeling, Northwestern University, Evanston, IL.
