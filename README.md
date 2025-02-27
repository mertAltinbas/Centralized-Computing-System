<h1>Client-Server Communication System (CCS)</h1>

<p>The Client-Server Communication System (CCS) is a Java-based application designed to facilitate communication between clients and a server using both TCP and UDP protocols. This project allows clients to perform arithmetic operations and receive results in real-time, while also providing service discovery capabilities through UDP.</p>

<h2>Key Features:</h2>
<ul>
    <li><strong>UDP Service Discovery:</strong> Clients can discover the server using a broadcast message, allowing for easy connection establishment.</li>
    <li><strong>TCP Communication:</strong> Clients can connect to the server via TCP to send arithmetic operation requests and receive results.</li>
    <li><strong>Arithmetic Operations:</strong> Supports basic operations such as addition, subtraction, multiplication, and division, with error handling for invalid inputs.</li>
    <li><strong>Statistics Reporting:</strong> The server tracks and reports statistics such as the number of clients, total requests, and operation counts at regular intervals.</li>
    <li><strong>Multi-threading:</strong> Utilizes a thread pool to handle multiple client connections concurrently, ensuring efficient processing.</li>
</ul>

<h2>Components:</h2>
<ul>
    <li><strong>CCS Class:</strong> The main server class that initializes the server, starts UDP and TCP services, and manages client connections.</li>
    <li><strong>ClientHandler Class:</strong> Handles individual client requests, processes arithmetic operations, and sends responses back to clients.</li>
    <li><strong>Statistics Class:</strong> Maintains and reports statistics related to client connections and operations performed.</li>
</ul>

<h2>Usage:</h2>
<ol>
    <li>Run the server application by specifying a port number.</li>
    <li>Use the client application to discover the server and connect to it.</li>
    <li>Send arithmetic operation requests in the format: <code>OPERATION ARG1 ARG2</code> (e.g., <code>ADD 5 10</code>).</li>
    <li>Receive and display the results of the operations.</li>
    <li>Monitor server statistics printed at regular intervals.</li>
</ol>

<h2>Requirements:</h2>
<ul>
    <li>Java Development Kit (JDK) 8 or higher</li>
</ul>

<h2>Installation:</h2>
<p>Clone the repository and compile the Java files. Ensure that the server is running before launching the client applications.</p>
