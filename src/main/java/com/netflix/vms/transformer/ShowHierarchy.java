package com.netflix.vms.transformer;

import com.netflix.hollow.util.HashCodes;
import com.netflix.hollow.util.IntList;
import com.netflix.vms.transformer.hollowinput.CountryVideoDisplaySetHollow;
import com.netflix.vms.transformer.hollowinput.EpisodeHollow;
import com.netflix.vms.transformer.hollowinput.SeasonHollow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShowHierarchy {

    private final int topNodeId;
    private final boolean isStandalone;
    private final int seasonIds[];
    private final int seasonSequenceNumbers[];
    private final int episodeIds[][];
    private final int episodeSequenceNumbers[][];
    private final int supplementalIds[];
    private final int hashCode;

    public ShowHierarchy(int topNodeId, boolean isStandalone, CountryVideoDisplaySetHollow set, String countryCode, ShowHierarchyInitializer initializer) {
        this.topNodeId = topNodeId;
        this.isStandalone = isStandalone;
        int hashCode = HashCodes.hashInt(topNodeId);

        IntList supplementalIds = new IntList();
        initializer.addSupplementalVideos(topNodeId, countryCode, supplementalIds);

        List<SeasonHollow> seasons = set._getChildren();
        if(seasons != null) {
            seasons = new ArrayList<SeasonHollow>(seasons);
            Collections.sort(seasons, new Comparator<SeasonHollow>() {
                public int compare(SeasonHollow o1, SeasonHollow o2) {
                    return (int)o1._getSequenceNumber() - (int)o2._getSequenceNumber();
                }
            });

            int seasonIds[] = new int[seasons.size()];
            int seasonSequenceNumbers[] = new int[seasons.size()];
            int episodeIds[][] = new int[seasons.size()][];
            int episodeSequenceNumbers[][] = new int[seasons.size()][];

            int seasonCounter = 0;
            int seasonSeqNum = 0;

            for(int i=0;i<seasons.size();i++) {
                SeasonHollow season = seasons.get(i);
                seasonSeqNum++;

                if(!initializer.isChildNodeIncluded(season._getMovieId(), countryCode))
                    continue;

                initializer.addSupplementalVideos(season._getMovieId(), countryCode, supplementalIds);

                seasonIds[seasonCounter] = (int)season._getMovieId();
                seasonSequenceNumbers[seasonCounter] = seasonSeqNum;
                hashCode ^= seasonIds[i];
                hashCode = HashCodes.hashInt(hashCode);

                List<EpisodeHollow> episodes = new ArrayList<EpisodeHollow>(season._getChildren());
                Collections.sort(episodes, new Comparator<EpisodeHollow>() {
                    public int compare(EpisodeHollow o1, EpisodeHollow o2) {
                        return (int)o1._getSequenceNumber() - (int)o2._getSequenceNumber();
                    }
                });

                episodeIds[seasonCounter] = new int[episodes.size()];
                episodeSequenceNumbers[seasonCounter] = new int[episodes.size()];

                int episodeCounter = 0;
                int episodeSeqNum = 0;

                for(int j=0;j<episodes.size();j++) {
                    EpisodeHollow episode = episodes.get(j);
                    episodeSeqNum++;

                    if(!initializer.isChildNodeIncluded(episode._getMovieId(), countryCode))
                        continue;

                    initializer.addSupplementalVideos(episode._getMovieId(), countryCode, supplementalIds);

                    episodeIds[seasonCounter][episodeCounter] = (int)episode._getMovieId();
                    episodeSequenceNumbers[seasonCounter][episodeCounter] = episodeSeqNum;
                    hashCode ^= episodeIds[seasonCounter][episodeCounter];
                    hashCode = HashCodes.hashInt(hashCode);
                    episodeCounter++;
                }

                if(episodeCounter != episodeIds[seasonCounter].length)
                    episodeIds[seasonCounter] = Arrays.copyOf(episodeIds[seasonCounter], episodeCounter);

                seasonCounter++;
            }

            if(seasonCounter != seasonIds.length) {
                seasonIds = Arrays.copyOf(seasonIds, seasonCounter);
                episodeIds = Arrays.copyOf(episodeIds, seasonCounter);
            }

            this.seasonIds = seasonIds;
            this.episodeIds = episodeIds;
            this.seasonSequenceNumbers = seasonSequenceNumbers;
            this.episodeSequenceNumbers = episodeSequenceNumbers;
        } else {
            this.seasonIds = new int[0];
            this.episodeIds = new int[0][];
            this.seasonSequenceNumbers = new int[0];
            this.episodeSequenceNumbers = new int[0][];
        }

        this.supplementalIds = supplementalIds.arrayCopyOfRange(0, supplementalIds.size());

        for(int i=0;i<supplementalIds.size();i++) {
            hashCode ^= HashCodes.hashInt(supplementalIds.get(i));
        }

        this.hashCode = hashCode;
    }

    public boolean isStandalone() {
        return isStandalone;
    }

    public int getTopNodeId() {
        return topNodeId;
    }

    public int[] getSeasonIds() {
        return seasonIds;
    }

    public int[] getSeasonSequenceNumbers() {
        return seasonSequenceNumbers;
    }

    public int[][] getEpisodeIds() {
        return episodeIds;
    }

    public int[][] getEpisodeSequenceNumbers() {
        return episodeSequenceNumbers;
    }

    public int[] getSupplementalIds() {
        return supplementalIds;
    }

    public boolean includesSupplementalId(int id) {
        for(int i=0;i<supplementalIds.length;i++)
            if(supplementalIds[i] == id)
                return true;
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ShowHierarchy))
            return false;
        ShowHierarchy other = (ShowHierarchy)obj;
        if(topNodeId != other.topNodeId)
            return false;
        if(!Arrays.equals(seasonIds, other.seasonIds))
            return false;
        for(int i=0;i<episodeIds.length;i++) {
            if(!Arrays.equals(episodeIds[i], other.episodeIds[i]))
                return false;
        }
        if(!Arrays.equals(supplementalIds, other.supplementalIds))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}