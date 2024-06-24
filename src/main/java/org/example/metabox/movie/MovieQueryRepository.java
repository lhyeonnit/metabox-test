package org.example.metabox.movie;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.example.metabox.user.UserResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class MovieQueryRepository {
    private final EntityManager em;

    // 마이페이지의 내 예매내역
    public List<UserResponse.MyPageHomeDTO.TicketingDTO> findMyTicketing(Integer sessionUserId) {
        String q = """
                SELECT m.title, m.img_filename, si.date as "관람일시", si.start_time as "시작시간", 
                si.end_time as "종료시간", b.id, s.name, t.name, b.user_id as "유저"
                FROM book_tb b
                INNER JOIN seat_book_tb sb ON sb.book_id = b.id
                INNER JOIN screening_info_tb si ON sb.screening_info_id = si.id
                INNER JOIN screening_tb s ON s.id = si.screening_id
                INNER JOIN theater_tb t ON s.theater_id = t.id
                INNER JOIN movie_tb m ON si.movie_id = m.id
                WHERE user_id = ?
                AND b.created_at >= CURRENT_DATE - INTERVAL '1' MONTH
                GROUP BY si.id          
                """;

        Query query = em.createNativeQuery(q);
        query.setParameter(1, sessionUserId);

        List<Object[]> rows = query.getResultList();
        List<UserResponse.MyPageHomeDTO.TicketingDTO> ticketingDTOList = new ArrayList<>();

        for (Object[] row : rows) {
            String title = (String) row[0];
            String imgFilename = (String) row[1];
            Date date = (Date) row[2];
            String startTime = (String) row[3];
            String endTime = (String) row[4];
            Integer bookId = (Integer) row[5];
            String name = (String) row[6];  // 몇관인지
            String theaterName = (String) row[7];  // 몇관인지
            Integer userId = ((Number) row[8]).intValue();

            UserResponse.MyPageHomeDTO.TicketingDTO ticketingDTO = UserResponse.MyPageHomeDTO.TicketingDTO.builder()
                    .title(title)
                    .imgFilename(imgFilename)
                    .date(date)
                    .startTime(startTime)
                    .endTime(endTime)
                    .id(bookId)
                    .name(name)
                    .theaterName(theaterName)
                    .userId(userId)
                    .build();

            ticketingDTOList.add(ticketingDTO);

        }

        return ticketingDTOList;


    }



    // 상영예정 영화 목록
    public List<UserResponse.MainChartDTO.ToBeChartDTO> getToBeChart() {

        String q = """
                select id, img_filename, title, start_date, DATEDIFF('DAY', CURRENT_DATE(), start_date) as d_day
                from movie_tb where start_date > CURRENT_DATE()
                """;

        Query query = em.createNativeQuery(q);
//        query.setParameter(1, 2);

        List<Object[]> rows = query.getResultList();
        List<UserResponse.MainChartDTO.ToBeChartDTO> movieChartDTOS = new ArrayList<>();

        for (Object[] row : rows) {
            Integer id = ((Number) row[0]).intValue();
            String imgFilename = (String) row[1];
            String title = (String) row[2];
            Date startDate = (Date) row[3];
            Integer dDay = ((Number) row[4]).intValue();    // integer로 변환시켜서 가져오기

            UserResponse.MainChartDTO.ToBeChartDTO toBeChartDTO = UserResponse.MainChartDTO.ToBeChartDTO.builder()
                    .id(id)
                    .imgFilename(imgFilename)
                    .title(title)
                    .startDate(startDate)
                    .dDay(dDay)
                    .build();

            movieChartDTOS.add(toBeChartDTO);

        }

        return movieChartDTOS;

    }



    // Main의 영화 받는 용
    public List<UserResponse.MainChartDTO.MainMovieChartDTO> getMainMovieChart() {

        String q = """
                select id, img_filename, title, movieCount * 1.0 / allCount as ticketSales
                from (select id, img_filename, title, start_date, 
                (select count(id) from seat_book_tb) as allCount,
                (select count(*) from seat_book_tb sb
                inner join screening_info_tb si on sb.screening_info_id = si.id
                where si.movie_id = m.id) as movieCount from movie_tb m) 
                as subquery order by ticketSales 
                desc limit 10
                   """;

        Query query = em.createNativeQuery(q);
//        query.setParameter(1, 2);

        List<Object[]> rows = query.getResultList();
        List<UserResponse.MainChartDTO.MainMovieChartDTO> movieChartDTOS = new ArrayList<>();

        for (Object[] row : rows) {
            Integer id = ((Number) row[0]).intValue();
            String imgFilename = (String) row[1];
            String title = (String) row[2];
            Double ticketSales = ((Number) row[3]).doubleValue() * 100;
            // 소수점 이하 두 자리까지 반올림
            ticketSales = Math.round(ticketSales * 100.0) / 100.0;

            UserResponse.MainChartDTO.MainMovieChartDTO movieChartDTO = UserResponse.MainChartDTO.MainMovieChartDTO.builder()
                    .id(id)
                    .imgFilename(imgFilename)
                    .title(title)
                    .ticketSales(ticketSales)
                    .build();

            movieChartDTOS.add(movieChartDTO);

        }

        return movieChartDTOS;

    }


    // 영화 받는 용
    public List<UserResponse.DetailBookDTO.MovieChartDTO> getMovieChart() {
//        String q = """
//                select id, img_filename, title, start_date, (select count(id) from seat_book_tb) as allCount,
//                (select count(*) from seat_book_tb sb
//                inner join screening_info_tb si on sb.screening_info_id = si.id
//                where si.movie_id = m.id) as movieCount,
//                (select count(*) from seat_book_tb sb
//                inner join screening_info_tb si on sb.screening_info_id = si.id
//                where si.movie_id = m.id) * 1.0 / (select count(id) from seat_book_tb sb) as ticketSales
//                from movie_tb m
//                """;

        String q = """
                select id, img_filename, title, start_date, movieCount * 1.0 / allCount as ticketSales
                from (select id, img_filename, title, start_date, 
                (select count(id) from seat_book_tb) as allCount,
                (select count(*) from seat_book_tb sb
                inner join screening_info_tb si on sb.screening_info_id = si.id
                where si.movie_id = m.id) as movieCount from movie_tb m) 
                as subquery order by ticketSales 
                desc limit 6
                   """;

        Query query = em.createNativeQuery(q);
//        query.setParameter(1, 2);

        List<Object[]> rows = query.getResultList();
        List<UserResponse.DetailBookDTO.MovieChartDTO> movieChartDTOS = new ArrayList<>();

        for (Object[] row : rows) {
            Integer id = ((Number) row[0]).intValue();
            String imgFilename = (String) row[1];
            String title = (String) row[2];
            Date startDate = (Date) row[3];
            Double ticketSales = ((Number) row[4]).doubleValue() * 100;
            // 소수점 이하 두 자리까지 반올림
            ticketSales = Math.round(ticketSales * 100.0) / 100.0;

            UserResponse.DetailBookDTO.MovieChartDTO movieChartDTO = UserResponse.DetailBookDTO.MovieChartDTO.builder()
                    .id(id)
                    .imgFilename(imgFilename)
                    .title(title)
                    .startDate(startDate)
                    .ticketSales(ticketSales)
                    .build();

            movieChartDTOS.add(movieChartDTO);

        }

        return movieChartDTOS;

    }


}
