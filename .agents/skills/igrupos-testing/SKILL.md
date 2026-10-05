---
name: "igrupos-testing"
description: "Unit testing patterns: Room DAOs with in-memory SQLCipher, ViewModels with fake repos, Hilt test modules."
metadata:
  version: "1.0.0"
  category: "project"
  tags: ["igrupos", "testing", "unit-tests", "room-testing", "viewmodel-testing"]
  triggers:
    include: ["igrupos testing", "test room dao igrupos", "test viewmodel igrupos", "write unit test igrupos", "mock repository test igrupos"]
  owners: ["@igrupos/maintainers"]
---

# IGrupos Testing

## Status
**Zero tests exist**. All testing infrastructure must be built from scratch.

## Recommended Stack

| Purpose | Library | Version (catalog) |
|---|---|---|
| Test runner | JUnit 5 (Jupiter) | via `junit` in libs |
| Mocking | MockK | via `mockk` in libs |
| Flow testing | Turbine | via `turbine` in libs |
| Room testing | `Room.inMemoryDatabaseBuilder` + SQLCipher | built-in |
| Hilt testing | `@HiltAndroidTest` + `HiltTestRunner` | via `hilt.android.testing` |
| Compose testing | `createComposeRule()` | via `compose.ui.test` |

## Room DAO Testing Pattern

```kotlin
class ClientDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ClientDao

    @Before
    fun setup() {
        System.loadLibrary("sqlcipher")
        val passphrase = "test".toByteArray()
        val factory = SupportOpenHelperFactory(passphrase)
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .openHelperFactory(factory)
            .build()
        dao = db.clientDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `insert and read client`() = runTest {
        val client = ClientEntity(name = "Test", cif = "B12345678", ...)
        val id = dao.insert(client)
        val loaded = dao.getById(id)
        assertEquals("Test", loaded?.name)
    }
}
```

## ViewModel Testing Pattern

```kotlin
class ClientesViewModelTest {
    private val fakeRepo = FakeClientRepository()
    private lateinit var viewModel: ClientesViewModel

    @Before
    fun setup() {
        viewModel = ClientesViewModel(fakeRepo)
    }

    @Test
    fun `search filters clients`() = runTest {
        fakeRepo.addClient(Client(name = "Empresa A", ...))
        fakeRepo.addClient(Client(name = "Otra S.L.", ...))

        viewModel.onSearchQueryChanged("Empresa")
        val clients = viewModel.clients.first()

        assertEquals(1, clients.size)
        assertEquals("Empresa A", clients[0].name)
    }
}
```

## Repository Testing Pattern

- Create a `FakeDao` implementing the DAO interface with in-memory `MutableList`
- Instantiate the Repository with the `FakeDao` and other fake dependencies
- Test each repository method for success and failure paths

## Test Location

Tests should be in each module's `src/test/` directory:
```
feature_clientes/src/test/java/com/igrupos/clientes/
data/src/test/java/com/igrupos/data/repository/
database/src/test/java/com/igrupos/database/dao/
```

## What to Prioritize

1. **DAOs** (room integrity, query correctness)
2. **Repositories** (business logic, error handling)
3. **ViewModels** (state transitions, validation, form save)
4. **Mappers** (entity→domain, domain→entity, JSON checklist serialization)
