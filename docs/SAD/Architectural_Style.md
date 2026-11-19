# Architectural style and placement of modules

## 1. Chosen style

EkoLagja uses hexagonal architecture, also known as ports and adapters, organised as a modular monolith.

In a hexagonal architecture the business logic of the application sits at the centre and does not depend on anything around it. The centre, called the domain, contains the concepts of the problem and their rules: a complaint, its status and how that status may change, a poll and the rule that a resident votes only once. Around the domain there are ports, which are interfaces that describe what the application offers to the outside world and what it needs from it. The technical parts of the system, such as the REST controllers used by the React frontend and the code that stores data in PostgreSQL, are adapters that plug into these ports.

The dependency rule is what holds the structure together: adapters depend on ports, ports and application services depend on the domain, and the domain depends on nothing. The domain does not know that a web interface or a database exists.

The whole system is built and deployed as one Spring Boot application with one PostgreSQL database. Inside that application the code is divided into four business modules, Complaints, Neighbourly Help, Notices and Polls, and a shared part. Each module is a small hexagon of its own, and the boundaries between modules are strict. This combination of one deployable application with clearly separated modules is called a modular monolith.

## 2. Why this style suits EkoLagja

EkoLagja connects residents with their municipality, so the platform handles official information: complaints that the municipality must answer, notices that residents rely on, and polls whose results should be trustworthy. The most important requirement on the architecture is therefore that the rules of the system are correct and stay correct as the system grows.

Hexagonal architecture keeps those rules in one place. A rule such as "a rejected complaint cannot be reopened" or "a resident can vote only once in a poll" is written in plain Java inside the domain, separate from controllers, SQL and framework code. Because the domain has no technical dependencies, these rules can be tested with ordinary unit tests, without starting a server or a database. This makes it practical to check the most important behaviour of the platform often.

The style also protects the system from changes in technology. A municipality may later want its own login system, another database, or a mobile application next to the website. In each case only an adapter changes or is added. A mobile application, for example, would be one more adapter calling the same ports as the React frontend, and the rules in the domain would stay untouched.

Every change to the data passes through a use case in the application layer. That gives one clear place to check whether the user is allowed to perform the action and to record what happened. For complaints this is where each status change is stored in the complaint's history, which matters for a public institution that must be able to show how a complaint was handled.

Finally, the style fits the way our team works. Each of the four members owns one module, and in a hexagonal structure a module contains everything from its domain classes to its controllers. Each member can therefore work on a complete part of the system with little interference from the others, and the inbound and outbound ports of a module correspond directly to the provided and required services described in the module's DeSCRIPTR specification.

## 3. Placement of the modules

Each module has the same internal structure:

    <module>/
        <Module>Api          the module's public entry point for other modules
        domain/              concepts and business rules
        application/
            port/in/         use cases the module offers
            port/out/        what the module needs from outside
            service/         implementation of the use cases
        adapter/
            in/web/          REST controllers used by the frontend
            out/persistence/ storage in PostgreSQL

Domain classes are kept separate from the classes used for storage. For example, Complaint is the domain class, while ComplaintJpaEntity is the version stored in the database, and a mapper converts between them. This keeps database details out of the domain.

The concepts of the conceptual model are distributed over the modules as follows:

| Module           | Owner            | Domain concepts                                                             |
|------------------|------------------|-----------------------------------------------------------------------------|
| Complaints       | Edin Kamberi     | Complaint, ComplaintCategory, StatusChange, OfficialResponse, InternalNote  |
| Neighbourly Help | Ediar Halili     | HelpPost, HelpResponse                                                      |
| Notices          | Erjon Demiri     | Notice, Attachment                                                          |
| Polls            | Endrit Abdullahu | Poll, PollOption, Vote                                                      |
| Shared part      | Whole group      | User, Resident, MunicipalOfficial, Administrator, Department, Neighbourhood |

The shared part is not counted as a module. It contains users and roles, login, the administration of neighbourhoods, departments and staff accounts, and the configuration of the application.

The Complaints module is shown in detail in the diagram [Complaints_Module_Hexagon.drawio](Complaints_Module_Hexagon.drawio). A resident submits a complaint from the React application, the request reaches ComplaintController, which calls the SubmitComplaint use case. ComplaintService carries out the use case: it creates the Complaint in the domain, asks the shared part through the ResponsibleStaff port which official is responsible for the complaint's category and neighbourhood, and saves the complaint through the Complaints port, which is implemented by the persistence adapter. The other three modules follow the same structure with their own concepts.

## 4. Communication between modules

A module never uses another module's domain classes, database tables or internal services directly. Modules communicate in two ways.

When a module needs an answer from another module, it calls that module's public entry point. Complaints, for example, asks the shared part for the official responsible for a neighbourhood and a category, and uses Notices to store the photos attached to a complaint.

When a module only needs to announce that something has happened, it publishes an event and does not care who reacts to it. When the status of a complaint changes, Complaints publishes a ComplaintStatusChanged event, and Notices reacts by notifying the resident who submitted it. In the same way, Neighbourly Help announces that a help post has received a response, and Polls announces that a poll has opened or closed. Notices is the module that turns these events into notifications for residents.

This means every module offers or uses at least one service of another module, while the modules stay loosely coupled. Complaints, Neighbourly Help and Polls do not depend on Notices at all; they only publish events.

The boundaries are checked automatically. A test built with Spring Modulith verifies that modules only use each other's public entry points, and an ArchUnit test verifies that domain classes do not depend on Spring, the persistence layer or any adapter. If someone breaks these rules, the build fails.

## 5. Alternatives considered

Two other styles were considered: a layered architecture and microservices.

A layered architecture divides the code into presentation, business logic and data access, where each layer calls the one below it. It is simple and familiar, but the business layer depends directly on the data layer. In practice this means business rules become mixed with database code, are harder to test in isolation, and the interfaces between modules are not explicit. For a system whose main value is the correctness of its rules, this was the weaker option.

A microservice architecture would turn each module into a separate application with its own database, connected through an API gateway and a message broker. This offers independent deployment and scaling, but it brings considerable operational cost: several applications to run, data spread over several databases, no foreign keys between modules, and failures in the network between services. EkoLagja serves one municipality and is built by a team of four, so these costs are not justified. The modular monolith keeps boundaries between modules that are just as clear, and if one module ever needs to become a separate service, its ports make that step possible without rewriting it.
