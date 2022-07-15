import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.BufferedWriter
import java.nio.file.Paths
import kotlin.io.path.writer

fun main() {
    val tsvReader = csvReader {
        delimiter = '\t'
        escapeChar = '#'
    }

    //Чтение файла csv
    tsvReader.open("strings.csv") {
        /* TODO/ нужно доделать создание strings_en и strings_fr в каждом модуле в зависимости от path модуля
        пока csv генерится на основе всего проекта, создает тут strings.csv и от него уже генерит strings_test.xml тут же
        */
//        val absolutePath = "common/common-data/src/commonMain/resources/strings/strings_ru.xml"
//        val formattedAbsolutePath = absolutePath.substringBefore("strings_ru")

        //Путь создания xml (пока в корне без директорий)
        val filePath = Paths.get("strings_test.xml")

        //Пишет в файл
        val writer = BufferedWriter(filePath.writer())
        val stringHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<resources>\n"

        writer.write(stringHeader)

        var prevPluralName = ""

        //перебор всех строк в таблице
        readAllAsSequence().forEachIndexed { i, row ->
            if (i != 0) {
                val path = row[0].split('§')[0]
                val pluralName = row[0].split('§')[1]
                val name = row[0].split('§')[2]

                //Выбор языка, который будет сгенерирован в xml
                val valueRu = row[0].split('§')[3].removeSurrounding("\"\"")
                val valueEn = row[0].split('§').getOrNull(4)
                val valueFr = row[0].split('§').getOrNull(5)

                if (pluralName.isNotEmpty() && prevPluralName.isEmpty()) {
                    writer.write("    <plurals name=\"$pluralName\">\n")
                    prevPluralName = pluralName
                }

                if (pluralName.isNotEmpty() && pluralName == prevPluralName) {
                    writer.write("        <item quantity=\"$name\">$valueRu</item>\n")
                }

                if (pluralName.isEmpty() && prevPluralName.isNotEmpty()) {
                    writer.write("    </plurals>\n")
                    prevPluralName = ""
                }

                if (pluralName.isEmpty()) {
                    writer.write("    <string name=\"$name\">$valueRu</string>\n")
                }
            }
        }

        writer.write("</resources>")
        writer.flush()
        writer.close()
    }
}
