# Резервная копия Cursor

Архив установки Cursor (`%LOCALAPPDATA%\Programs\cursor`), создан 2026-05-25.

## Состав

| Файл | Размер |
|------|--------|
| `cursor-backup.7z.001` … `003` | по 100 MiB |
| `cursor-backup.7z.004` | ~28 MiB |

Всего 4 тома, ~329 MiB. Хранятся через Git LFS.

## Восстановление

1. Скачать все тома в одну папку.
2. Установить [7-Zip](https://www.7-zip.org/).
3. Распаковать первый том (7-Zip соберёт многотомный архив автоматически):

```powershell
& "${env:ProgramFiles}\7-Zip\7z.exe" x ".\cursor-backup.7z.001" -o"C:\Users\Ekaterina\AppData\Local\Programs\cursor"
```

4. При необходимости переустановить Cursor поверх распакованной папки.

## Создание архива (для справки)

```powershell
& "${env:ProgramFiles}\7-Zip\7z.exe" a -t7z -v100m -mx=5 "cursor-backup.7z" "$env:LOCALAPPDATA\Programs\cursor"
```
