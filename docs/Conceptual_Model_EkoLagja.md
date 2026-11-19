# Conceptual model – EkoLagja

Diagram: [Conceptual_Model_EkoLagja.drawio](Conceptual_Model_EkoLagja.drawio)

## Overview

EkoLagja is a digital platform that connects residents with their municipality. The municipality publishes notices and runs polls, residents submit complaints and follow how they are handled, and neighbours can ask for or offer help within their neighbourhood.

The conceptual model shows the concepts of the problem domain and how they relate. It does not describe the program's classes, so the classes have attributes only and no operations.

The model has 18 concepts. The three kinds of users, `Resident`, `MunicipalOfficial` and `Administrator`, inherit from `User`. `Neighbourhood` ties the platform together: residents live in a neighbourhood, and notices, complaints and help posts belong to one.

## Concepts

| Concept | Description | Attributes |
|---|---|---|
| `User` | Anyone who uses the platform. | firstName, lastName, email, phone |
| `Resident` | A resident of a neighbourhood: submits complaints, votes and helps neighbours. | address, registrationDate |
| `MunicipalOfficial` | A municipal employee who publishes notices, handles complaints and creates polls. | position, employeeNumber |
| `Administrator` | A municipal administrator who keeps the platform's data up to date: neighbourhoods, departments, complaint categories and staff accounts. | appointedAt |
| `Department` | A department of the municipality (for example Public Works) that employs officials. | name, phone, email |
| `Neighbourhood` | A neighbourhood of the municipality. | name, municipality |
| `Notice` | An official announcement from the municipality to residents. | title, content, category, publishedAt, expiresOn, isUrgent |
| `Attachment` | A file (photo or document) attached to a notice or a complaint. | fileName, fileType, uploadedAt |
| `Complaint` | A problem a resident reports to the municipality. Only the resident who submitted it and municipal staff can see it. | title, description, address, submittedAt, status, latitude, longitude |
| `ComplaintCategory` | The kind of problem a complaint is about (roads, lighting, water...). | name, description |
| `StatusChange` | One step in a complaint's history: from which status to which, when, and why. | fromStatus, toStatus, changedAt, note |
| `OfficialResponse` | The municipality's final answer to a complaint. | text, respondedAt |
| `InternalNote` | A note staff write on a complaint while handling it. The resident never sees it. | text, createdAt |
| `Poll` | A question the municipality asks residents to get their opinion. | question, description, startDate, endDate, isAnonymous |
| `PollOption` | One of the possible answers to a poll. | text |
| `Vote` | A resident's choice in a poll. | votedAt |
| `HelpPost` | A request for or an offer of help between neighbours. | type, title, description, category, status, publishedAt |
| `HelpResponse` | A resident's reply to a help post, offering to help or asking for the offered help. | message, respondedAt, status |

Allowed values of some attributes:

- `Complaint.status`, `StatusChange.fromStatus`, `StatusChange.toStatus` (type `ComplaintStatus`): Received, In progress, Resolved, Rejected. A new complaint starts as Received.
- `Complaint.latitude`, `Complaint.longitude` (multiplicity 0..1): an optional map pin for where the problem is.
- `HelpPost.type`: request (the resident needs help) or offer (the resident offers help).
- `HelpPost.status`: open, in progress, closed.
- `HelpResponse.status`: offered, accepted, declined. The author of the post accepts or declines a response.

## Inheritance (is-a)

- **`Resident` is a `User`**
- **`MunicipalOfficial` is a `User`**
- **`Administrator` is a `User`**

All three have a first name, last name, email and phone, but their roles on the platform differ. The shared attributes are on `User`, and each kind adds its own.

## Aggregation (part-whole)

| Whole – part | Multiplicity | Explanation |
|---|---|---|
| `Department` employs `MunicipalOfficial` | 1 – * | A department is made up of its officials. Every official works in one department. |
| `Poll` contains `PollOption` | 1 – 2..* | The options are part of the poll. Every poll has at least two options. |
| `Notice` has `Attachment` | 0..1 – * | A notice can have files attached. |
| `Complaint` has `Attachment` | 0..1 – * | A resident can attach photos of the problem. An attachment belongs to either a notice or a complaint. |
| `Complaint` records `StatusChange` | 1 – * | The status history is part of the complaint and shows how it was handled. |
| `Complaint` has `InternalNote` | 1 – * | Staff notes are part of the complaint's handling and are kept with it. |

## Associations

| Association | Multiplicity | Explanation |
|---|---|---|
| `MunicipalOfficial` publishes `Notice` | 1 – * | Every notice has one official who published it. |
| `Notice` addressed to `Neighbourhood` | * – 0..* | A notice can be addressed to several neighbourhoods. With none, it applies to the whole municipality. |
| `Resident` submits `Complaint` | 1 – * | Every complaint has one resident who submitted it. |
| `MunicipalOfficial` handles `Complaint` | 0..1 – * | A new complaint is not yet assigned to an official. |
| `Complaint` belongs to `ComplaintCategory` | * – 1 | Every complaint has one category, which helps send it to the right department. |
| `ComplaintCategory` handled by `Department` | * – 1 | Each category is handled by one department, for example roads by Public Works. |
| `MunicipalOfficial` responsible for `Neighbourhood` | * – 1..* | Every official covers one or more neighbourhoods. A new complaint goes to an official of the category's department who covers the complaint's neighbourhood. |
| `Complaint` receives `OfficialResponse` | 1 – 0..1 | A complaint gets at most one final response, and none while it is still being handled. |
| `Complaint` located in `Neighbourhood` | * – 1 | Every complaint belongs to one neighbourhood. |
| `MunicipalOfficial` writes `InternalNote` | 1 – * | Every internal note has one official who wrote it. |
| `MunicipalOfficial` creates `Poll` | 1 – * | Every poll has one official who created it. |
| `Resident` casts `Vote` | 1 – * | Every vote belongs to one resident. |
| `Vote` for `PollOption` | * – 1 | Every vote is for exactly one option. |
| `Resident` publishes `HelpPost` | 1 – * | Every help post has one resident who published it. |
| `HelpPost` receives `HelpResponse` | 1 – * | A help post can get several responses from neighbours. |
| `Resident` sends `HelpResponse` | 1 – * | Every response has one resident who sent it. |
| `HelpPost` posted in `Neighbourhood` | * – 1 | Help is offered within one neighbourhood. |
| `Resident` lives in `Neighbourhood` | * – 1 | Every resident lives in one neighbourhood. |
| `Administrator` registers `MunicipalOfficial` | 1 – * | Every staff account is created by an administrator. |
| `Administrator` manages `Department` | 1..* – * | Administrators add and change departments and the categories each one handles. |
| `Administrator` manages `Neighbourhood` | 1..* – * | Administrators add new neighbourhoods as the municipality changes. |

## Mapping to modules

The group has 4 members, so the system has 4 modules:

| Module | Concepts in the model |
|---|---|
| Complaints | `Complaint`, `ComplaintCategory`, `StatusChange`, `OfficialResponse`, `InternalNote` |
| Neighbourly Help | `HelpPost`, `HelpResponse` |
| Notices | `Notice`, `Attachment` |
| Polls | `Poll`, `PollOption`, `Vote` |

`User`, `Resident`, `MunicipalOfficial`, `Administrator`, `Department` and `Neighbourhood` are used by every module and belong to the group's shared work. `Attachment` is owned by Notices and reused by Complaints.

There is one administrator role per municipality. A super administrator managing several municipalities is not needed while the platform serves one municipality.
