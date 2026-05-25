# Резервная копия Cursor (для переноса на другой ПК)

Архив собран из `%LOCALAPPDATA%\Programs\cursor` (настройки, расширения, кэш, логи и т.д.).

## Содержимое

| Файл | Размер |
|------|--------|
| `cursor-backup.part01.rar` … `part03.rar` | по 100 MiB |
| `cursor-backup.part04.rar` | ~28 MiB |

**Всего ~4,3 ГБ** (5 томов RAR).

## На другом ПК

1. Клонировать репозиторий: `git clone https://github.com/Evgenii-Mikhin/minio.git`
2. Установить [Git LFS](https://git-lfs.github.com): `git lfs install`, затем `git lfs pull`
3. Установить [WinRAR](https://www.win-rar.com/) (если ещё нет): `winget install WinRAR`
4. Распаковать **все тома** в одну папку:
   - ПКМ по **`cursor-backup.part01.rar`** → «Извлечьь сюда» → `%LOCALAPPDATA%\Programs\cursor`
   - Либо в PowerShell из папки `cursor-backup`:
   ```powershell
   & "${env:ProgramFiles}\WinRAR\WinRAR.exe" x "cursor-backup.part01.rar" -o"$env:LOCALAPPDATA\Programs\cursor"
   ```
5. Запустить Cursor — настройки подтянутся из распакованной папки.

## Git LFS

В репозитории тома хранятся через Git LFS. Без `git lfs pull` вместо архива будут маленькие указатели (~130 байт).

## Если «Не удалось открыть файл»

- Убедитесь, что скачаны **все** части `part01`–`part05`, не только `.001` от LFS.
- Распаковывать с **первого** тома (`part01.rar`), остальные подтянутся автоматически.
- Не открывать `.rar` как ZIP в Проводнике — только через WinRAR или 7-Zip с томами `.7z.001`.