# Conceptual model – EkoLagja

Diagram: [Conceptual_Model_EkoLagja.drawio](Conceptual_Model_EkoLagja.drawio) (preview: [PNG](Conceptual_Model_EkoLagja.png))

## Overview

EkoLagja is a digital platform that connects residents with their municipality. The municipality publishes notices and runs polls, residents submit complaints and follow how they are handled, and neighbours can ask for or offer help within their neighbourhood.

The conceptual model shows the concepts of the problem domain and how they relate. It does not describe the program's classes, so the classes have attributes only and no operations.

The two kinds of users, `Resident` and `MunicipalOfficial`, inherit from `User`. `Neighbourhood` is the concept that ties everything together: residents live in a neighbourhood, and notices, complaints and help posts belong to one.

## Concepts

| Concept | Description | Attributes |
|---|---|---|
| `User` | Anyone who uses the platform. | firstName, lastName, email, phone |
| `Resident` | A resident of a neighbourhood: submits complaints, votes and helps neighbours. | address, registrationDate |
| `MunicipalOfficial` | A municipal employee who publishes notices, handles complaints and creates polls. | position, department |
| `Neighbourhood` | A neighbourhood of the municipality where residents live. | name, municipality |
| `Notice` | An official announcement from the municipality to residents. | title, content, category, publishedAt, expiresOn, isUrgent |
| `Complaint` | A problem a resident reports to the municipality. | title, description, category, address, submittedAt, status, officialResponse |
| `Poll` | A question the municipality asks residents to get their opinion. | question, description, startDate, endDate, isAnonymous |
| `PollOption` | One of the possible answers to a poll. | text |
| `Vote` | A resident's choice in a poll. | votedAt |
| `HelpPost` | A request for or an offer of help between neighbours. | type, title, description, category, status, publishedAt |

Allowed values of some attributes:

- `Complaint.status` (type `ComplaintStatus`): Received, In progress, Resolved, Rejected. A new complaint starts as Received.
- `HelpPost.type`: request (the resident needs help) or offer (the resident offers help).
- `HelpPost.status`: open, in progress, closed.

## Inheritance (is-a)

- **`Resident` is a `User`**
- **`MunicipalOfficial` is a `User`**

Both have a first name, last name, email and phone, but their roles on the platform differ. The shared attributes are on `User`, and each kind adds its own.

## Aggregation (part-whole)

- **`Poll` contains `PollOption`** (1 – 2..*): the options are part of the poll. Every poll has at least two options.

## Associations

| Association | Multiplicity | Explanation |
|---|---|---|
| `MunicipalOfficial` publishes `Notice` | 1 – * | Every notice has one official who published it. |
| `Notice` addressed to `Neighbourhood` | * – 0..* | A notice can be addressed to several neighbourhoods. With none, it applies to the whole municipality. |
| `Resident` submits `Complaint` | 1 – * | Every complaint has one resident who submitted it. |
| `MunicipalOfficial` handles `Complaint` | 0..1 – * | A new complaint is not yet assigned to an official. |
| `Complaint` located in `Neighbourhood` | * – 1 | Every complaint belongs to one neighbourhood. |
| `MunicipalOfficial` creates `Poll` | 1 – * | Every poll has one official who created it. |
| `Resident` casts `Vote` | 1 – * | Every vote belongs to one resident. |
| `Vote` for `PollOption` | * – 1 | Every vote is for exactly one option. |
| `Resident` publishes `HelpPost` | 1 – * | Every help post has one resident who published it. |
| `Resident` helps with `HelpPost` | 0..1 – * | The resident who responds to the post. A new post has no response yet. |
| `HelpPost` posted in `Neighbourhood` | * – 1 | Help is offered within one neighbourhood. |
| `Resident` lives in `Neighbourhood` | * – 1 | Every resident lives in one neighbourhood. |

## Mapping to modules

The group has 4 members, so the system has 4 modules:

| Module | Concepts in the model |
|---|---|
| Notices | `Notice` |
| Complaints | `Complaint` |
| Polls | `Poll`, `PollOption`, `Vote` |
| Neighbourly Help | `HelpPost` |

`User`, `Resident`, `MunicipalOfficial` and `Neighbourhood` are used by every module and belong to the group's shared work.
