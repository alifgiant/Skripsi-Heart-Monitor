import numpy as np


class BeatCategory(object):
    normal = {'code': 1, 'name': "Normal", 'annotation': ['N', 'P', 'f', 'p', 'L', 'R',' Q']}
    pvc = {'code': 2, 'name': "PVC", 'annotation': ['V']}
    vf = {'code': 3, 'name': "Ventricular Flutter/Fibrillation", 'annotation': ['[', '!', ']']}
    HeartBlock = {'code': 4, 'name': "Heart Block", 'annotation': ['(BII']}


class RhythmCategory (object):
    n = {'name': 'Normal'}
    b = {'name': 'Ventricular Bigeminy'}
    c = {'name': 'Ventricular Couplets'}
    t = {'name': 'Ventricular Trigeminy'}
    vt = {'name': 'Ventricular Tachycardia'}
    vfl = {'name': 'Ventricular Flutter/fibrillation'}
    BII = {'name': 'heart block'}


class Classifier(object):
    '''
    RR-i window with size 3
    @param {Array} segment
    @param {int} i
    @param {int} n
    @returns {*} an element of segment
    '''
    @staticmethod
    def __get_rr(segment, i, n):
        if n == 1:
            return segment[i]  # 0
        elif n == 2:
            return segment[i+1] # 1
        elif n == 3:
            return segment[i+2]  # 2
        else:
            raise Exception("RR window is 1 to 3")

    '''
    @return {boolean}
    '''
    @staticmethod
    def __C1(RR1, RR2, RR3):
        return RR2 < 0.6 and 1.8*RR2 < RR1

    '''
    @return {boolean}
    '''
    @staticmethod
    def __C2(RR1, RR2, RR3):
        return (RR1 < 0.7 and RR2 < 0.7 and RR3 < 0.7) or (RR1+RR2+RR3 < 1.7)

    '''
    @return {boolean}
    '''
    @staticmethod
    def __C3(RR1, RR2, RR3):
        return 1.15*RR2 < RR1 and 1.15*RR2 < RR3

    '''
    @return {boolean}
    '''
    @staticmethod
    def __C4(RR1, RR2, RR3):
        return (abs(RR1-RR2)) < 0.3 and (RR1 < 0.8 or RR2 < 0.8) and (RR3 > 1.2 * np.mean([RR1, RR2]))

    '''
    @return {boolean}
    '''
    @staticmethod
    def __C5(RR1, RR2, RR3):
        return (abs(RR2-RR3)) < 0.3 and (RR2 < 0.8 or RR3 < 0.8) and (RR1 > 1.2*np.mean([RR2, RR3]))

    '''
    @return {boolean}
    '''
    @staticmethod
    def __C6(RR1, RR2, RR3):
        return (2.2 < RR2 and RR2 < 3.0) and (abs(RR1-RR2)<0.2 or abs(RR2-RR3)<0.2)

    '''
     * @param {float[]} segment 32 element array
     * @returns {Array} of category
     * category:
     * 1 = Normal
     * 2 = PVC
     * 3 = VF
     * 4 = Heart Block
     */
    '''
    @staticmethod
    def analyze_beat_segment(rr_segment):
        i = 0  # first window
        segment_length = len(rr_segment)
        category = []

        while i < segment_length - 2:
            RR1 = Classifier.__get_rr(rr_segment, i, 1)
            RR2 = Classifier.__get_rr(rr_segment, i, 2)
            RR3 = Classifier.__get_rr(rr_segment, i, 3)

            # category[i] = BeatCategory.normal  # set category normal
            category.append(BeatCategory.normal) # set category normal

            if Classifier.__C1(RR1, RR2, RR3):
                pulseCount = 0
                while True:
                    category.append(BeatCategory.vf)  # set category Ventricular Flutter/Fibrillation
                    i += 1  # move to next windows
                    pulseCount += 1

                    # get next window RRs
                    RR1 = Classifier.__get_rr(rr_segment, i, 1)
                    RR2 = Classifier.__get_rr(rr_segment, i, 2)
                    RR3 = Classifier.__get_rr(rr_segment, i, 3)
                    # print 'before', category
                    if i < segment_length - 2 and Classifier.__C2(RR1, RR2, RR3):  # ignore last 1 element
                        break
                if pulseCount < 4:
                    while pulseCount > 0:
                        i -= 1
                        pulseCount -= 1
                        category.append(BeatCategory.normal)  # set category normal
            if Classifier.__C3(RR1, RR2, RR3) and Classifier.__C4(RR1, RR2, RR3) and Classifier.__C5(RR1, RR2, RR3):
                category.append(BeatCategory.pvc)  # set category PVC
            if Classifier.__C6(RR1, RR2, RR3):
                category.append(BeatCategory.HeartBlock)  # set category Heart Block

            i += 1  # move to next windows
        return category

    '''
    /**
     * @param {int} state
     * @param {BeatCategory} beat
     * @returns {int} next state
     */
    '''
    @staticmethod
    def __finite_state_automation(state, beat):
        if state == 1:  # Initial stage of the automaton.
            if beat == BeatCategory.pvc:
                state = 2
            elif beat == BeatCategory.vf:
                state = 7
            elif beat == BeatCategory.HeartBlock:
                state = 8
            else:  # normal
                state = 1
        elif state == 2:  # Possible ventricular bigeminy, trigeminy, couplet or tachycardia.
            if beat == BeatCategory.normal:
                state = 3
            elif beat == BeatCategory.pvc:
                state = 5
            else:
                state = Classifier.__finite_state_automation(1, beat)
        elif state == 3:
            if beat == BeatCategory.pvc:
                state = 2
            elif beat == BeatCategory.normal:
                state = 4
            else:
                state = Classifier.__finite_state_automation(1, beat)
        elif state == 4:
            if beat == BeatCategory.pvc:
                state = 2
            else:
                state = Classifier.__finite_state_automation(1, beat)
        elif state == 5:
            if beat == BeatCategory.pvc:
                state = 6
            elif beat == BeatCategory.normal:
                state = 3
            else:
                state = Classifier.__finite_state_automation(1, beat)
        elif state == 6:
            if beat == BeatCategory.pvc:
                state = 6
            elif beat == BeatCategory.vf:
                state = 7
            elif beat == BeatCategory.HeartBlock:
                state = 8
            elif beat == BeatCategory.normal:
                state = 3
            else:
                state = Classifier.__finite_state_automation(1, beat)
        elif state == 7:
            if beat == BeatCategory.pvc:
                state = 2
            elif beat == BeatCategory.vf:
                state = 7
            elif beat == BeatCategory.HeartBlock:
                state = 8
            else:
                state = Classifier.__finite_state_automation(1, beat)
        elif state == 8:
            if beat == BeatCategory.pvc:
                state = 2
            elif beat == BeatCategory.vf:
                state = 7
            elif beat == BeatCategory.HeartBlock:
                state = 8
            else:
                state = Classifier.__finite_state_automation(1, beat)
        else:
            state = 1
        return state

    '''
    /**
     * @param {BeatCategory[]} segment
     * @return {RhythmCategory[]} rhythm
     */
    '''
    @staticmethod
    def analyze_beat_classification(segment):
        i = 0
        episodes = [RhythmCategory.n]

        next_state = 1
        # last_state = 1
        vf = 0
        bigeminy = 0
        trigeminy = 0
        block = 0

        while i < len(segment):
            beat = segment[i]
            last_state = next_state
            next_state = Classifier.__finite_state_automation(next_state, beat)
            if next_state == 2 and last_state == 3:
                bigeminy += 1
                if bigeminy == 2:
                    episodes.append(RhythmCategory.b)
                trigeminy = 0
                vf = 0
                block = 0
            elif next_state == 2 and last_state == 4:
                trigeminy += 1
                if trigeminy == 2:
                    episodes.append(RhythmCategory.t)
                bigeminy = 0
                vf = 0
                block = 0
            elif next_state == 5:
                episodes.append(RhythmCategory.c)
                bigeminy = 0
                trigeminy = 0
                vf = 0
                block = 0
            elif next_state == 6:
                if last_state != 6:
                    episodes.append(RhythmCategory.vt)
                bigeminy = 0
                trigeminy = 0
                vf = 0
                block = 0
            elif next_state == 7:
                vf += 1
                if vf == 3:
                    episodes.append(RhythmCategory.vfl)
                bigeminy = 0
                trigeminy = 0
                block = 0
            elif next_state == 8:
                block += 1
                if block == 2:
                    episodes.append(RhythmCategory.BII)
                bigeminy = 0
                trigeminy = 0
                vf = 0
            i += 1
        return episodes
