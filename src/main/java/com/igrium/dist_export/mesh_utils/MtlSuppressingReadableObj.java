package com.igrium.dist_export.mesh_utils;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
import de.javagl.obj.ReadableObj;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MtlSuppressingReadableObj implements ReadableObj {

    private final ReadableObj base;

    public MtlSuppressingReadableObj(ReadableObj base) {
        this.base = base;
    }

    @Override
    public int getNumVertices() {
        return base.getNumVertices();
    }

    @Override
    public FloatTuple getVertex(int index) {
        return base.getVertex(index);
    }

    @Override
    public int getNumTexCoords() {
        return base.getNumTexCoords();
    }

    @Override
    public FloatTuple getTexCoord(int index) {
        return null; // Suppressed
    }

    @Override
    public int getNumNormals() {
        return base.getNumNormals();
    }

    @Override
    public FloatTuple getNormal(int index) {
        return null; // Suppressed
    }

    @Override
    public int getNumFaces() {
        return base.getNumFaces();
    }

    @Override
    public ObjFace getFace(int index) {
        return base.getFace(index);
    }

    @Override
    public Set<String> getActivatedGroupNames(ObjFace face) {
        return base.getActivatedGroupNames(face);
    }

    @Override
    public String getActivatedMaterialGroupName(ObjFace face) {
        return base.getActivatedMaterialGroupName(face);
    }

    @Override
    public int getNumGroups() {
        return base.getNumGroups();
    }

    @Override
    public ObjGroup getGroup(int index) {
        return base.getGroup(index);
    }

    @Override
    public ObjGroup getGroup(String name) {
        return base.getGroup(name);
    }

    @Override
    public int getNumMaterialGroups() {
        return base.getNumMaterialGroups();
    }

    @Override
    public ObjGroup getMaterialGroup(int index) {
        return base.getMaterialGroup(index);
    }

    @Override
    public ObjGroup getMaterialGroup(String name) {
        return base.getMaterialGroup(name);
    }

    @Override
    public List<String> getMtlFileNames() {
        return Collections.emptyList(); // Suppressed
    }
}