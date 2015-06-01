Network Coding based Peer to Peer File Sharing
============================================
Shares files within private peer to peer network, gaining maximum throughput by chunk sharing and network coding. 
This is branch of [P2P Network coding](https://github.com/TheNilesh/p2p-file-sharing), which shares everything except RMI approach.

Working of System
-----------------------
The file is splitted into blocks. Peer initiating downloading gets file blocks simultaneously from multiple peers. However peer cant contact other peer directly, the central Supeepeer coordinates the connection among peers. Hence the problem of NAT traversal is alleviated by using **UDP Hole Punching**. The server decides route and network coding applicability.
[WORKING](https://drive.google.com/open?id=0B55XUQq2E5CjRGg3ZVgwclFBZlk&authuser=0)

Network Coding
-----------------
I am using paircoding.
If we use RNC, It is required to encode a message in a very large field, so it reduces the probability of failing to decode messages. An other drawback of random network coding is the increase in the data traffic. As there is no deterministic path for data delivery, all the nodes take part in relaying the data to the receiver is even if it is not necessary. As a result, the same message may be transmitted through the same link multiple times.

References
----------
http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6497042&url=http%3A%2F%2Fieeexplore.ieee.org%2Fiel7%2F12%2F4358213%2F06497042.pdf%3Farnumber%3D6497042

TODO
---------
0. Logging - to terminal, file, and GUI
0. Icons, ProgressBar in JTable
0. XOR-ing blocks (Network Coding), File subscribe function
0. Network Overlay  -- Dynamic Ring of SuperPeers, Peers connect closest Superpeer.

Deferred Bugs
----------------
0. No authentication, and encryption.