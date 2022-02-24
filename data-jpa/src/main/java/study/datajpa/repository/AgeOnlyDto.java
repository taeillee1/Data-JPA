package study.datajpa.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AgeOnlyDto {

    private final String username;
    private final int age;

}
