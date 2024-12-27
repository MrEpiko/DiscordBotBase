package me.mrepiko.discordbotbase.mics;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PaginationResponse<T> extends ArrayList<T> {

    private final int pageSize;

    public PaginationResponse(int pageSize) {
        this(pageSize, new ArrayList<>());
    }

    public PaginationResponse(int pageSize, List<T> objects) {
        this.pageSize = pageSize;
        addAll(objects);
    }

    public static <T> PaginationResponse<T> of(int pageSize) {
        return new PaginationResponse<>(pageSize);
    }

    public static <T> PaginationResponse<T> of(int pageSize, List<T> objects) {
        return new PaginationResponse<>(pageSize, objects);
    }

    public static <T> PaginationResponse<T> empty(int pageSize) {
        return new PaginationResponse<>(pageSize);
    }

    public int pageSize() {
        return pageSize;
    }

    public int totalPages() {
        return (int) Math.ceil((double) size() / pageSize);
    }

    public boolean hasNextPage(int page) {
        return page < totalPages();
    }

    public boolean hasPreviousPage(int page) {
        return page > 1;
    }

    public List<T> getPage(int page) {
        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= size()) return new ArrayList<>();
        int toIndex = Math.min(fromIndex + pageSize, size());
        return subList(fromIndex, toIndex);
    }

    public boolean pageExists(int page) {
        return page > 0 && page <= totalPages();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

}
