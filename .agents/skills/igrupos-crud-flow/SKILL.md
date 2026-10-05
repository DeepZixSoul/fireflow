---
name: "igrupos-crud-flow"
description: "Standardized CRUD pattern: navigation flow, ViewModel state management, validation, and Repository patterns."
metadata:
  version: "1.0.0"
  category: "project"
  tags: ["igrupos", "crud", "patterns", "viewmodel"]
  triggers:
    include: ["igrupos crud", "create client", "create revision", "form validation", "viewmodel pattern igrupos"]
  owners: ["@igrupos/maintainers"]
---

# IGrupos CRUD Flow

## Navigation Pattern

The project uses Navigation Compose with 16 routes defined in `Routes` object (`ui/src/main/java/.../navigation/`).

Standard screen triad:
1. **List screen** → shows items, FAB to create, click to detail
2. **Detail screen** → shows item info, action buttons (edit, curves, photos, PDF, export)
3. **Form screen** → create/edit with validation, saves and pops back

Routes follow REST-like pattern:
```
client/{clientId}
client/{clientId}/groups
client/{clientId}/group/{groupId}
client/{clientId}/group/{groupId}/revisions
revision/{revisionId}
```

Arguments are passed via path params or optional query params (for forms with optional edit mode):
```
client/form?clientId={clientId}   → clientId nullable (null = create)
```

## ViewModel State Pattern

Every form ViewModel follows the same structure:
```kotlin
data class XxxFormState(
    // Form fields
    val field1: String = "",
    val field2: String = "",
    // Validation
    val field1Error: String? = null,
    // Controls
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class XxxFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: XxxRepositoryImpl
) : ViewModel() {
    private val entityId: Long? = savedStateHandle.get<String>("entityId")?.toLongOrNull()
    private val _uiState = MutableStateFlow(XxxFormState())
    val uiState: StateFlow<XxxFormState> = _uiState.asStateFlow()

    init {
        if (entityId != null) loadEntity()
    }

    fun save() {
        // 1. Validate required fields
        // 2. If errors, set fieldXxxError and return
        // 3. isSaving = true
        // 4. repository.save(entity)
        // 5. On success: isSaved = true (Screen observes this → popBackStack)
        // 6. On failure: error = message
    }
}
```

## Validation Rules

- **Client**: `name` required, `cif` required (uppercased)
- **Group**: All text fields optional (brand, model, serial number, etc.)
- **Revision**: `technicianName` used but not validated as required
- **Curve**: At least one point with valid numeric flow + pressure

## Repository Pattern

- Repositories have `saveXxx(entity)` which branches on `entity.id == 0L` → INSERT else UPDATE
- Repositories return `Result<T>` for one-shot operations and `Flow<List<T>>` for reactive lists
- DAOs use `@Upsert` (Room 2.6+) or `@Insert(onConflict = REPLACE)` + `@Update`

## List Screen Pattern

- Uses `flatMapLatest` switch between full list and search query
- Auto-refreshes because Room Flow emits on data changes
- Standard `SharingStarted.WhileSubscribed(5000)`
