# Notes for the architecture section of the SAD

These are my notes, not the final text. I write the real section from them in my own words.


## 1. What we decided

We use hexagonal architecture, also called "ports and adapters".

The whole app is still one Spring Boot program with one MySQL database. Inside it, the code is split into 4 modules (Complaints, Help, Notices, Polls) plus a shared part. That setup is called a "modular monolith": one program, but with clear walls between the modules.

The teacher allowed three styles: layered, microservices, hexagonal. We picked hexagonal.


## 2. The idea in simple words

Think of each module as having three rings:

- The centre (domain): the real rules of the app, written in plain Java. For example "a rejected complaint can't be reopened" or "a resident can vote only once in a poll". This part knows nothing about Spring, the database or the website.
- Ports: interfaces (Java interfaces) around the centre. Some ports are for others to call us, like "submit a complaint". Some are for things we need from outside, like "save this complaint".
- Adapters: the real technical code that plugs into the ports. The REST controller that the React app calls is an adapter. The code that saves to MySQL is also an adapter.

The rule: the outside depends on the inside, never the other way around. The centre never imports anything from Spring or the database.


## 3. Why this is a good fit (points to explain in my own words)

- The important rules stay in one clean place, so they're easy to check and test without starting a database.
- If the municipality later wants a different database, its own login system or a mobile app, we only swap or add an adapter. The rules stay the same.
- Each module clearly shows what it offers and what it needs. That is exactly the "I" (interface) part of DeSCRIPTR, so the SAD and the code match.
- Every change goes through one use case, so it's easy to check who is allowed to do it and to keep a history (StatusChange). Good for a municipal system.
- Each of us owns one module from top to bottom, so our work and our commits stay separate.


## 4. What real projects do (from the research)

I looked at real public projects: OpenTripPlanner, Eclipse EDC, ddd-by-examples/library, buckpal and the Spring Modulith examples. Things they have in common:

- They split the code by module first, and put the hexagon inside each module.
- Each module has a small "front door" (a public class plus the events it sends). Other modules may only use that front door. Everything else is private to the module.
- They keep the database classes separate from the real classes. Example: Complaint is the real class, ComplaintJpaEntity is the database version, and a small mapper converts between them. This keeps database code out of the centre.
- They don't overdo ports, and they name them after real things. Example: "Complaints" instead of "ComplaintRepositoryPort".
- They have tests that fail if someone breaks the module walls.
- Their architecture write-ups are short (about 2 to 4 pages), mostly normal paragraphs, with a couple of real diagrams.


## 5. Folder structure

Each module looks the same. Example for Complaints (mine):

    complaints/
        ComplaintsApi         the front door other modules use
        domain/               Complaint, ComplaintCategory, StatusChange, OfficialResponse
        application/
            port/in/          what we offer: SubmitComplaintUseCase, ChangeComplaintStatusUseCase ...
            port/out/         what we need: Complaints (saving), ResidentDirectory (finding residents)
            service/          the code that does the use cases
        adapter/
            in/web/           ComplaintController (REST for the React app)
            out/persistence/  saving to MySQL: ComplaintJpaEntity, ComplaintMapper ...

The other modules have the same shape:
- help/ (Ediar)
- notices/ (Erjon)
- polls/ (Endrit)

shared/ has users, neighbourhoods, departments, login and settings.

Naming, in short:
- Things we offer end in UseCase.
- The code behind them ends in Service.
- REST classes end in Controller.
- Database classes end in JpaEntity, and the converters end in Mapper.


## 6. What each module offers and needs (first idea, everyone can rename theirs)

Complaints
- Offers: submit, assign to an official, change status, give official answer, list complaints
- Needs: saving complaints, finding residents, storing photos (from Notices)

Neighbourly Help
- Offers: publish a post, answer a post, accept or decline an answer, close a post, list posts
- Needs: saving posts, finding residents

Notices
- Offers: publish a notice, list notices, notify residents, store attachments
- Needs: saving notices, storing files, finding neighbourhoods

Polls
- Offers: create a poll, vote, close a poll, show results
- Needs: saving polls, finding residents


## 7. How modules talk to each other

Two ways:

1. Asking something: call the other module's front door directly. Example: Complaints asks Notices to store a photo.
2. Telling something happened: send an event. The module that sends it doesn't care who listens.
   - A complaint's status changes → Notices tells the resident.
   - Someone answers a help post → Notices tells the post's author.
   - A poll opens or closes → Notices announces it and later shows the results.

This also covers the course rule that every module must use or offer something to another module.


## 8. How we make sure nobody breaks the rules

- Spring Modulith has a test that checks modules only use each other's front door.
- One ArchUnit test checks that the centre (domain) doesn't use Spring or database code.
- If someone breaks a rule, the build fails.


## 9. Why not the other two styles

- Layered: the business code depends directly on the database code, so rules and database code get mixed together. It's harder to test and the module boundaries aren't clear.
- Microservices: every module would be its own program with its own database, plus a gateway and a message system. That's a lot of extra work and moving parts for an app this size and a team of four. With our setup the walls between modules are just as clear, and a module can still be split out later if ever needed.


## 10. Still to make

- Diagram 1 (draw.io): the 4 modules plus shared, with arrows for calls and events.
- Diagram 2 (draw.io): the inside of one module (Complaints): controller → use cases → domain → saving → MySQL.
- Maybe short decision notes, one page each:
  - why hexagonal and not layered or microservices
  - why events for notifications
  - why separate database classes


## Links

- buckpal: https://github.com/thombergs/buckpal
- ddd-by-examples/library: https://github.com/ddd-by-examples/library
- Spring Modulith: https://docs.spring.io/spring-modulith/reference/fundamentals.html
- modular-monolith-with-ddd: https://github.com/kgrzybek/modular-monolith-with-ddd
- OpenTripPlanner: https://github.com/opentripplanner/OpenTripPlanner/blob/dev-2.x/ARCHITECTURE.md
- Eclipse EDC decision records: https://github.com/eclipse-edc/Connector/tree/main/docs/developer/decision-records
