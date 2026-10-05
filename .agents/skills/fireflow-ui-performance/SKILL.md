---
name: "fireflow-ui-performance"
description: "UI optimization: Paging 3, chart strategy, image loading, lazy lists, accessibility."
metadata:
  version: "1.0.0"
  category: "project"
  tags: ["fireflow", "performance", "paging", "charts", "accessibility", "ui-optimization"]
  triggers:
    include: ["fireflow performance", "paging fireflow", "chart fireflow", "lazy list fireflow", "accessibility fireflow"]
  owners: ["@fireflow/maintainers"]
---

# FireFlow UI & Performance

## Charts (Current Status)

Vico 2.0.0-alpha.19 was integrated but **removed** due to API incompatibility. Current curve screens show data as text summary instead of charts.

**Decision needed**: Fix Vico (update to stable or correct alpha API) or replace. Options:
- **Vico** (re-evaluate with compatible API)
- **Custom Canvas drawing** (PdfDocument-style Paint API)
- **YCharts** or other Compose chart libs

Until resolved, curve screens display tabular data without charts.

## Paging 3 (Planned)

`ClientDao.getAllActive()` returns `Flow<List<ClientEntity>>` — no Paging 3.

For 10k+ clients target, must migrate to Paging 3:
```kotlin
// DAO
@Query("SELECT * FROM clients WHERE isActive = 1 ORDER BY name ASC")
fun getAllActivePaged(): PagingSource<Int, ClientEntity>

// Repository
fun getClientsPaged(): Flow<PagingData<Client>> = Pager(PagingConfig(pageSize = 50)) {
    dao.getAllActivePaged().map { it.toDomain() }
}.flow

// ViewModel uses .cachedIn(viewModelScope)
// Screen uses collectAsLazyPagingItems()
```

⚠️ **Not urgent** until dataset grows beyond 1000 records. Current `Flow<List<T>>` is fine for initial deployment.

## Image Loading

- **Coil** for Compose: `AsyncImage(model = File(filePath), contentDescription = ...)`
- Photos are compressed to max 1920px on save
- EXIF metadata is stripped (also removes orientation info; ensure consistent display)
- Stored in `filesDir/photos/{revisionId}/{filename}` — no manual cache management needed
- For lists/grids: consider generating smaller thumbnails for grid view (future optimization)

## Lazy List Optimization

Current implementation uses:
- `LazyColumn` with `items()` or `itemsIndexed()`
- No `key` parameter on all lists → add for better diffing
- No `contentType` — not critical yet
- StaggeredGrid for photos with `LazyVerticalStaggeredGrid`

**Rules:**
- Always provide `key = { it.id }` on `items()` for stable recomposition
- Use `Modifier.drawWithContent` or `Modifier.graphicsLayer` for heavy animations
- Avoid unnecessary `State` reads inside list items

## Accessibility

Current state: **no accessibility beyond default Compose behavior**.

Minimum requirements:
- All icons must have `contentDescription` (text or `null` for decorative)
- Form fields need labels (already provided via `label` parameter in `OutlinedTextField`)
- Buttons should describe action: `"Crear cliente"` not just icon
- Color contrast: red primary (#D32F2F) on white passes WCAG AA for large text, but may fail for small text; black (#1A1A1A) on white passes AA
- Consider `semantics {}` for custom composables

## Performance Checklist

- [ ] Add `key` to all `LazyColumn` items
- [ ] Verify `derivedStateOf` used for computed values in composables
- [ ] Check `remember` for expensive computations
- [ ] Profile composition counts with Layout Inspector
- [ ] Test with 500+ client dataset
- [ ] Verify Coil disk cache is working (default 250MB)
