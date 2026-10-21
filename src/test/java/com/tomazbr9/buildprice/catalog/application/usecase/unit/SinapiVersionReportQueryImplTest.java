package com.tomazbr9.buildprice.catalog.application.usecase.unit;

import com.tomazbr9.buildprice.catalog.application.port.out.SinapiTableVersionRepository;
import com.tomazbr9.buildprice.catalog.application.port.out.StateRepository;
import com.tomazbr9.buildprice.catalog.application.usecase.SinapiVersionReportQueryImpl;
import com.tomazbr9.buildprice.catalog.domain.entity.SinapiTableVersion;
import com.tomazbr9.buildprice.catalog.domain.entity.State;
import com.tomazbr9.buildprice.catalog.domain.enums.TaxReliefRegime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SinapiVersionReportQueryImplTest {

    @Mock
    private SinapiTableVersionRepository versionRepository;

    @Mock
    private StateRepository stateRepository;

    @InjectMocks
    private SinapiVersionReportQueryImpl query;

    @Test
    void shouldReturnHistoricalSinapiVersionData() {

        UUID versionId = UUID.randomUUID();
        UUID stateId = UUID.randomUUID();

        SinapiTableVersion version =
                SinapiTableVersion.restore(
                        versionId,
                        stateId,
                        YearMonth.of(2026, 9),
                        TaxReliefRegime.NOT_EXEMPTED,
                        LocalDate.of(2026, 9, 10)
                );

        State state = State.restore(
                stateId,
                "SP",
                "São Paulo"
        );

        when(versionRepository.findById(versionId))
                .thenReturn(Optional.of(version));

        when(stateRepository.findById(stateId))
                .thenReturn(Optional.of(state));

        var result = query.findById(versionId);

        assertTrue(result.isPresent());

        assertEquals("SP", result.get().stateAbbreviation());

        assertEquals(
                YearMonth.of(2026, 9),
                result.get().referenceMonth()
        );

        assertEquals(
                "NOT_EXEMPTED",
                result.get().taxReliefRegime()
        );

        verify(versionRepository).findById(versionId);
        verify(stateRepository).findById(stateId);
    }

    @Test
    void shouldReturnEmptyWhenVersionDoesNotExist() {

        UUID versionId = UUID.randomUUID();

        when(versionRepository.findById(versionId))
                .thenReturn(Optional.empty());

        var result = query.findById(versionId);

        assertTrue(result.isEmpty());

        verifyNoInteractions(stateRepository);
    }
}
