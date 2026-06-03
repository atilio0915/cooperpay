package cooperpay.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class SemanaUtils {

    public static String getSemanaAtual() {
        LocalDate agora = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int semana = agora.get(weekFields.weekOfWeekBasedYear());
        int ano = agora.getYear();
        return ano + "-W" + String.format("%02d", semana);
    }

    public static String formatarSemanaComDatas(String semanaIso) {
        if (semanaIso == null || !semanaIso.contains("-W")) {
            return semanaIso;
        }
        try {
            String[] parts = semanaIso.split("-W");
            int ano = Integer.parseInt(parts[0]);
            int semana = Integer.parseInt(parts[1]);

            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            // Obtém o primeiro dia da semana (Segunda-feira) para a semana informada
            LocalDate inicio = LocalDate.of(ano, 1, 1)
                    .with(weekFields.weekOfWeekBasedYear(), semana)
                    .with(weekFields.dayOfWeek(), 1);

            LocalDate fim = inicio.plusDays(6);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

            return "Semana " + semana + " (" + inicio.format(fmt) + " a " + fim.format(fmt) + ")";
        } catch (Exception e) {
            return semanaIso;
        }
    }
}