package bookmarks;

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

    private final BookmarkRepository bookmarkRepository;

    private final AccountRepository accountRepository;

    BookmarkRestController(BookmarkRepository bookmarkRepository,
                           AccountRepository accountRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    Resources<BookmarkResource> readBookmarks(@PathVariable String userId) {

        this.validateUser(userId);

        List<BookmarkResource> bookmarkResourceList = bookmarkRepository
                .findByAccountUsername(userId).stream().map(BookmarkResource::new)
                .collect(Collectors.toList());

        return new Resources<>(bookmarkResourceList);
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {

        this.validateUser(userId);

        return accountRepository.findByUsername(userId)
                .map(account -> {
                    Bookmark bookmark = bookmarkRepository
                            .save(new Bookmark(account, input.uri, input.description));

                    Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");

                    return ResponseEntity.created(URI.create(forOneBookmark.getHref())).build();
                })
                .orElse(ResponseEntity.noContent().build());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{bookmarkId}")
    BookmarkResource readBookmark(@PathVariable String userId,
                                  @PathVariable Long bookmarkId) {
        this.validateUser(userId);
        return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
    }

    private void validateUser(String userId) {
        this.accountRepository
                .findByUsername(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}