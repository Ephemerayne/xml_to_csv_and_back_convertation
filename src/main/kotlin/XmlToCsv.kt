import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.QuoteMode
import org.apache.commons.lang3.StringUtils.EMPTY
import java.io.BufferedWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.writer

fun main() {

    //Путь к проекту
    val directory = File("../../StudioProjects/lmru--mobile-magfront-mpp").toPath()
    val files = Files.walk(directory)

    //Создание (пересоздание, удаление) файла табличного вида из всех strings_ru LMWork в корне
    val filePath = Paths.get("strings.csv")
    if (filePath.exists()) {
        Files.delete(filePath)
        Files.createFile(filePath)
    } else {
        Files.createFile(filePath)
    }

    /*Настройка формата записи и запись в файл заголовков столбцов
    Делимитер принудительно выставлен в виде '§' - так как по дефолту это ',', из-за чего в строках ресурсов
    обрезались значения с запятой
     */
    val writer = BufferedWriter(filePath.writer())
    val csvFormat = CSVFormat.DEFAULT.withDelimiter('§')
    val csvPrinter = CSVPrinter(
        writer,
        csvFormat.withHeader(
            "path",
            "plural_name",
            "name",
            "value_ru",
            "value_en",
            "value_fr"
        )
    )

    //Перебираем все strings_ru в LMWork, исключаем build пэкэджи (т.к. в них тоже генерится strings_ru)
    files.forEach { path ->
        if (path.toString().contains("strings_ru.xml") && !path.toString().contains("build")) {

            //Относительный путь strings_ru в каждом из модулей
            val relativePath = path.toString().split("lmru--mobile-magfront-mpp/").last()

            var pluralName = ""

            //Построчное чтение из xml и операции по извлечению имени ресурса и значения
            Files.readAllLines(path).forEach {
                if (it.contains("<string name=")) {
                    val name = it.substringAfter('"').substringBefore('"')
                    val value = it.substringAfter('>').substringBefore("</")
                    csvPrinter.printRecord(relativePath, pluralName, name, value)
                }
                if (it.contains("<plurals name=")) {
                    pluralName = it.substringAfter('"').substringBefore('"')
                }
                if (it.contains("</plurals>")) {
                    pluralName = ""
                }
                if (it.contains("<item quantity=")) {
                    val itemName = it.substringAfter('"').substringBefore('"')
                    val itemValue = it.substringAfter('>').substringBefore("</")
                    csvPrinter.printRecord(relativePath, pluralName, itemName, itemValue)
                }
            }
        }
    }
    csvPrinter.flush()
    csvPrinter.close()
}