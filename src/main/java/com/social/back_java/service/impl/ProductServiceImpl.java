    }

    @Override
    public Product updateProduct(Long id, Product product) {
        if (productRepository.existsById(id)) {
            product.setId(id);
            return productRepository.save(product);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Product addImagesToProduct(Long productId, List<MultipartFile> images) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (images == null || images.isEmpty()) {
            return productRepository.save(product);
        }
        for (MultipartFile image : images) {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(image);
            
            ProductImage productImage = new ProductImage();
            productImage.setUrl((String) uploadResult.get("secure_url"));
            productImage.setPublicId((String) uploadResult.get("public_id"));
            productImage.setAlt(product.getName());
            productImage.setPrimary(product.getImages().isEmpty()); // First image is primary
            
            product.getImages().add(productImage);
        }

        return productRepository.save(product);
    }

    @Override
    public Product addVideosToProduct(Long productId, List<MultipartFile> videos) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (videos == null || videos.isEmpty()) {
            return productRepository.save(product);
        }
        for (MultipartFile video : videos) {
            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(video);
            
            ProductVideo productVideo = new ProductVideo();
            productVideo.setUrl((String) uploadResult.get("secure_url"));
            productVideo.setPublicId((String) uploadResult.get("public_id"));
            productVideo.setThumbnail((String) uploadResult.get("thumbnail_url"));
            
            if (uploadResult.get("duration") != null) {
                productVideo.setDuration(((Number) uploadResult.get("duration")).doubleValue());
            }
            
            product.getVideos().add(productVideo);
        }

        return productRepository.save(product);
    }

    @Override
    public Product removeImageFromProduct(Long productId, String publicId) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.getImages().removeIf(image -> image.getPublicId().equals(publicId));
        cloudinaryService.deleteFile(publicId);

        return productRepository.save(product);
    }

    @Override
    public Product removeVideoFromProduct(Long productId, String publicId) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.getVideos().removeIf(video -> video.getPublicId().equals(publicId));
        cloudinaryService.deleteFile(publicId);

        return productRepository.save(product);
    }
}
