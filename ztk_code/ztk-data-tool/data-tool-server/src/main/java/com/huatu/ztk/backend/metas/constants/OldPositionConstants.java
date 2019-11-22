package com.huatu.ztk.backend.metas.constants;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.paper.bean.Position;
import com.huatu.ztk.paper.common.PositionConstants;
import org.springframework.beans.BeanUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangqp on 2018\1\18 0018.
 */
public class OldPositionConstants {
    public static final List<Position> POSITION_LIST = new LinkedList<>();

    static {
        InputStream input = PositionConstants.class.getClassLoader().getResourceAsStream("position.txt");
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            bufferedReader.lines().forEach(line -> {
                final String[] split = line.split(",");
                int id = Integer.valueOf(split[0]);
                String name = split[1];
                int parent = Integer.valueOf(split[2]);
                if (parent == 0) {
                    Position position = new Position(id, name, 0, Lists.newLinkedList());
                    POSITION_LIST.add(position);
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public static final List<Position> getPositions() {
        return POSITION_LIST;
    }

    public static final String getFullPositionName(int positionId) {
        for (Position position : POSITION_LIST) {
            if (position.getId() == positionId) {
                return position.getName();
            }

            final List<Position> childrens = position.getChildrens();

            for (Position children : childrens) {
                if (children.getId() == positionId) {
                    return position.getName() + children.getName();
                }
            }
        }
        return "未知职位";
    }


    public static final Position getPosition(int positionId) {
        for (Position position : POSITION_LIST) {
            if (position.getId() == positionId) {
                return position;
            }

            final List<Position> childrens = position.getChildrens();
            for (Position children : childrens) {
                if (children.getId() == positionId) {
                    return children;
                }
            }
        }

        return null;
    }


    public static final int getParentId(int positionId) {
        final Position position = getPosition(positionId);
        if (position != null) {
            return position.getParent();
        }
        return -1;
    }

}
